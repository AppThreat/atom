package io.appthreat.atom

import better.files.File
import io.appthreat.c2cpg.testfixtures.{DataFlowCodeToCpgSuite, DataFlowTestCpg}
import io.appthreat.edg2atom.parser.EdgaRunner
import io.appthreat.x2cpg.passes.taggers.{
    AllocationStatePass,
    ExtentPass,
    GuardPass,
    IntegerWidthPass,
    MemoryApiPass,
    MemorySafetyFindingPass,
    ValueOriginPass
}
import io.circe.parser.parse
import io.shiftleft.semanticcpg.language.*

import java.io.{ByteArrayOutputStream, PrintStream}

/** The `memory-safety` command end to end: run the overlay the way `Atom.applyTaggers` does, then
  * render through [[MemorySafetyCommands]] and read the JSON back.
  *
  * The renderer has no detector logic in it, so what is worth asserting here is the CONTRACT a
  * scorer reads (rule, cwe, kind, file, line, confidence) and the evidence rows the flow exists to
  * carry - the origin, guard and extent facts each rule actually consulted. Both of those shipped
  * broken once: the evidence entries were built and thrown away, and `Tag.value` is a String, so
  * iterating it rendered `struct-field` as twelve single-letter rows.
  */
class MemorySafetyCommandTests extends DataFlowCodeToCpgSuite:

  private val cpg: DataFlowTestCpg = new DataFlowTestCpg()
      .moreCode(
        """
        |#include <string.h>
        |#include <stdint.h>
        |
        |struct data_block { unsigned char *payload; int payload_len; };
        |
        |/* MS-BOUND-001: the caller's capacity parameter is destroyed before the write. */
        |int overwrites_size_param(unsigned char *buf, int size, struct data_block *db)
        |{
        |    size = db->payload_len;
        |    memcpy(buf, db->payload, size);
        |    return size;
        |}
        |
        |/* Honours the contract: nothing to report. */
        |int honours_size_param(unsigned char *buf, int size, struct data_block *db)
        |{
        |    memcpy(buf, db->payload, size);
        |    return size;
        |}
        |
        |/* MS-BOUND-002: an externally reachable helper copying a caller-chosen count. */
        |void unbounded_copy(unsigned char *buf, const unsigned char *src, int n)
        |{
        |    memcpy(buf, src, n);
        |}
        |
        |/* MS-BOUND-003/004: attacker index, known capacity, no bound. */
        |int index_read(int i)
        |{
        |    int buf[8];
        |    return buf[i];
        |}
        |
        |void index_write(int n, unsigned char v)
        |{
        |    unsigned char buf[8];
        |    buf[n] = v;
        |}
        |
        |/* MS-INT-002: the guard tests the re-signed view (CVE-2026-75145 shape). */
        |int resign_guard(uint32_t obu_size, int remaining, unsigned char *dst,
        |                 const unsigned char *src)
        |{
        |    if ((long)obu_size > remaining)
        |        return -1;
        |    memcpy(dst, src, obu_size);
        |    return 0;
        |}
        |""".stripMargin,
        "copy.c"
      )
      .moreCode(
        """
        |#include <stdlib.h>
        |#include <stdio.h>
        |
        |/* MS-ALLOC-003 at the implicit end of the function: the fact lands on METHOD_RETURN,
        |   which is a CFG_NODE and not an Expression. */
        |void leaks_at_the_implicit_end(void)
        |{
        |    char *p = (char *)malloc(64);
        |    p[0] = 'x';
        |}
        |
        |/* MS-ALLOC-003 anchored at a bare `return;` - the finding sits on the RETURN node. */
        |void leaks_at_a_bare_return(int n)
        |{
        |    char *p = (char *)malloc(64);
        |    if (n < 0) return;
        |    free(p);
        |}
        |
        |/* the unbraced `if (p) free(p);`: a narrowing that misses the false edge renders this
        |   negative control as a leak at the implicit end */
        |void unbraced_if_frees(char *unused, size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    if (p) free(p);
        |}
        |
        |/* MS-ALLOC-001 and MS-ALLOC-002 in one function. */
        |void double_free_then_use(void)
        |{
        |    char *p = (char *)malloc(32);
        |    free(p);
        |    free(p);
        |    p[0] = 'x';
        |}
        |
        |/* MS-ALLOC-004. */
        |void double_close(const char *path)
        |{
        |    FILE *f = fopen(path, "r");
        |    if (f == NULL) return;
        |    fclose(f);
        |    fclose(f);
        |}
        |""".stripMargin,
        "alloc.c"
      )
      .moreCode(
        """
        |#include <stdlib.h>
        |#include <string.h>
        |
        |struct node { struct node *next; int v; };
        |
        |/* MS-NULL-001 arm 1 (medium): an unchecked allocation result. */
        |void null_deref_unchecked_malloc(size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    p[0] = 'a';
        |    free(p);
        |}
        |
        |/* MS-NULL-001 arm 3 (the per-finding low tier): an unvalidated parameter through a
        |   SELF-REFERENTIAL traversal hop (next has head's own pointee type). */
        |void null_deref_chained_param(struct node *head)
        |{
        |    int v = head->next->v;
        |    (void)v;
        |}
        |
        |/* an unchecked allocation through a helper - the callee's derefs-param summary is what
        |   makes handing it an unchecked allocation a dereference */
        |static int head_byte(char *s)
        |{
        |    return s[0];
        |}
        |
        |void null_deref_via_helper_summary(size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    int c = head_byte(p);
        |    (void)c;
        |    free(p);
        |}
        |
        |/* the helper reads through nothing, so the same statement is not a
        |   dereference and must not render */
        |static void just_log(char *p)
        |{
        |    printf("%p\n", (void *)p);
        |}
        |
        |void no_null_deref_via_nonreading_helper(size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    just_log(p);
        |    free(p);
        |}
        |""".stripMargin,
        "null.c"
      )
      .moreCode(
        """
        |#include <string.h>
        |
        |/* MS-ESC-001: the finding is anchored at the RETURN - an exit node, not a length
        |   argument; a renderer that only matched arguments would silently drop every one. */
        |char *returns_stack_array(void)
        |{
        |    char buf[32];
        |    strcpy(buf, "hello");
        |    return buf;
        |}
        |""".stripMargin,
        "escape.c"
      )
      .moreCode(
        """
        |#include <stdlib.h>
        |#include <string.h>
        |
        |typedef struct hdr { size_t size; } hdr_t;
        |
        |#define PREFIX_SIZE (sizeof(size_t))
        |
        |/* An allocator that hands its block out past a size header: the block
        |   starts one header before the pointer the caller holds, so the pair's
        |   own release moves back onto the header before freeing. */
        |
        |/* free((hdr_t *)p - 1): the move back onto the header sits in the call */
        |void good_free_inline_header(void *p)
        |{
        |    if (p == NULL) return;
        |    free((hdr_t *)p - 1);
        |}
        |
        |/* p--; free(p): the pointer steps back onto the header, then releases */
        |void good_free_after_decrement(void *p)
        |{
        |    hdr_t *h = (hdr_t *)p;
        |    if (h == NULL) return;
        |    h--;
        |    free(h);
        |}
        |
        |/* p -= 1; free(p) */
        |void good_free_after_subtract_assign(void *p)
        |{
        |    hdr_t *h = (hdr_t *)p;
        |    if (h == NULL) return;
        |    h -= 1;
        |    free(h);
        |}
        |
        |/* free((char *)ptr - PREFIX_SIZE): a macro names the offset */
        |void good_free_macro_prefix(void *ptr)
        |{
        |    if (ptr == NULL) return;
        |    free((char *)ptr - PREFIX_SIZE);
        |}
        |
        |/* a local copy of the parameter, moved back inside the release call */
        |void good_free_copy_inline_offset(void *p)
        |{
        |    if (p == NULL) return;
        |    void *copy = p;
        |    free((hdr_t *)copy - 1);
        |}
        |
        |/* a local copy of the parameter, subtracted then released */
        |void good_free_copy_after_subtract_assign(void *p)
        |{
        |    if (p == NULL) return;
        |    hdr_t *copy = (hdr_t *)p;
        |    copy -= 1;
        |    free(copy);
        |}
        |
        |/* The releases the rule still owns: each frees off the start of a block
        |   this frame allocated. */
        |
        |/* arithmetic forward off a fresh allocation */
        |void bad_free_plus_one(size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    if (p == NULL) return;
        |    memset(p, 0, n);
        |    free(p + 1);
        |}
        |
        |/* a walk forward over an allocation this frame holds, then the release */
        |void bad_free_after_walk(const char *userInput)
        |{
        |    char *p = (char *)malloc(64);
        |    if (p == NULL) return;
        |    strncpy(p, userInput, 63);
        |    p[63] = 0;
        |    while (*p)
        |        p++;
        |    free(p);
        |}
        |
        |/* advanced by one amount, moved back by a smaller one: still off the start */
        |void bad_free_back_after_larger_advance(size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    if (p == NULL) return;
        |    memset(p, 0, n);
        |    p = p + 4;
        |    free(p - 1);
        |}
        |
        |/* a walk over a pointer no allocation in this frame produced: the release
        |   is the caller's contract, not this rule's */
        |void silent_walk_then_free(char *data)
        |{
        |    while (*data)
        |        data++;
        |    free(data);
        |}
        |""".stripMargin,
        "offsetfree.c"
      )
      .moreCode(
        """
        |#include <stdlib.h>
        |#include <string.h>
        |
        |/* The terminator write: the allocation added a constant's room to the
        |   very length the write indexes, so the store of the NUL lands inside
        |   the buffer the room was sized for. */
        |
        |/* buf[len] after malloc(len + 1): the write the room was sized for */
        |void good_terminator_write(const char *userInput)
        |{
        |    size_t len = strlen(userInput);
        |    char *buf = (char *)malloc(len + 1);
        |    if (buf == NULL) return;
        |    memcpy(buf, userInput, len);
        |    buf[len] = 0;
        |    free(buf);
        |}
        |
        |/* the index written through a cast of the same variable */
        |void good_terminator_cast_index(const char *userInput)
        |{
        |    size_t len = strlen(userInput);
        |    char *buf = (char *)malloc(len + 1);
        |    if (buf == NULL) return;
        |    memcpy(buf, userInput, len);
        |    buf[(int)len] = 0;
        |    free(buf);
        |}
        |
        |/* the size expression through a copy of the variable, the index the original */
        |void good_terminator_copy_size(const char *userInput)
        |{
        |    size_t len = strlen(userInput);
        |    size_t n = len;
        |    char *buf = (char *)malloc(n + 1);
        |    if (buf == NULL) return;
        |    memcpy(buf, userInput, len);
        |    buf[len] = 0;
        |    free(buf);
        |}
        |
        |/* the index through a copy of the variable, the size expression the original */
        |void good_terminator_copy_index(const char *userInput)
        |{
        |    size_t len = strlen(userInput);
        |    size_t idx = len;
        |    char *buf = (char *)malloc(len + 1);
        |    if (buf == NULL) return;
        |    memcpy(buf, userInput, len);
        |    buf[idx] = 0;
        |    free(buf);
        |}
        |
        |/* an index the allocation never measured */
        |void bad_terminator_unrelated_index(const char *userInput, size_t m)
        |{
        |    size_t len = strlen(userInput);
        |    char *buf = (char *)malloc(len + 1);
        |    if (buf == NULL) return;
        |    memcpy(buf, userInput, len);
        |    buf[m] = 0;
        |    free(buf);
        |}
        |
        |/* the measured variable, rewritten between the allocation and the write */
        |void bad_terminator_len_rewritten(const char *userInput, size_t extra)
        |{
        |    size_t len = strlen(userInput);
        |    char *buf = (char *)malloc(len + 1);
        |    if (buf == NULL) return;
        |    memcpy(buf, userInput, len);
        |    len += extra;
        |    buf[len] = 0;
        |    free(buf);
        |}
        |
        |/* the measured variable plus the same constant the room added: one past the
        |   room, never reported by the index arm on a variable-plus-constant index */
        |void silent_terminator_plus_one(const char *userInput)
        |{
        |    size_t len = strlen(userInput);
        |    char *buf = (char *)malloc(len + 1);
        |    if (buf == NULL) return;
        |    memcpy(buf, userInput, len);
        |    buf[len + 1] = 0;
        |    free(buf);
        |}
        |""".stripMargin,
        "terminator.c"
      )
      .moreCode(
        """
        |#include <stdlib.h>
        |#include <string.h>
        |
        |struct rec {
        |    char *name;
        |    int refs;
        |};
        |
        |static struct rec *rec_new(const char *name)
        |{
        |    struct rec *r = (struct rec *)calloc(1, sizeof(*r));
        |    if (r == NULL) return NULL;
        |    r->name = strdup(name);
        |    r->refs = 1;
        |    return r;
        |}
        |
        |/* a destroy: frees the members, then the argument, behind a NULL guard on
        |   the argument. Whenever the caller hands it a named block, the release
        |   runs. */
        |void rec_destroy_guarded(struct rec *r)
        |{
        |    if (r == NULL) return;
        |    free(r->name);
        |    free(r);
        |}
        |
        |/* releases only when the flag says the object is ours */
        |void rec_release_if_owned(struct rec *r, int owned)
        |{
        |    if (owned) {
        |        free(r->name);
        |        free(r);
        |    }
        |}
        |
        |/* frees the argument when its reference count reaches zero */
        |void rec_unref(struct rec *r)
        |{
        |    if (--r->refs == 0)
        |        free(r);
        |}
        |
        |/* a use of the object after the guarded destroy */
        |void bad_use_after_guarded_destroy(void)
        |{
        |    struct rec *r = rec_new("session");
        |    if (r == NULL) return;
        |    rec_destroy_guarded(r);
        |    (void)r->refs;
        |}
        |
        |/* the flag kept ownership with the caller, so the object is still live */
        |int good_use_after_flag_gated_release(void)
        |{
        |    struct rec *r = rec_new("borrowed");
        |    int v;
        |    if (r == NULL) return -1;
        |    rec_release_if_owned(r, 0);
        |    v = r->refs;
        |    rec_release_if_owned(r, 1);
        |    return v;
        |}
        |
        |/* a second reference is held, so the count-gated release keeps the object
        |   live until the last unreference */
        |int good_use_after_counter_gated_release(void)
        |{
        |    struct rec *r = rec_new("shared");
        |    int v;
        |    if (r == NULL) return -1;
        |    r->refs++;
        |    rec_unref(r);
        |    v = r->refs;
        |    rec_unref(r);
        |    return v;
        |}
        |""".stripMargin,
        "destroy.c"
      )
      .moreCode(
        """
        |#include <stdio.h>
        |
        |/* the fixture's own prototype hands a stream to the descriptor release,
        |   whose real parameter is an int: the file family released by the fd
        |   family's call */
        |int close(void *desc);
        |
        |int stream_closed_with_close(const char *path)
        |{
        |    FILE *f = fopen(path, "r");
        |    if (f == NULL)
        |        return -1;
        |    close(f);
        |    return 0;
        |}
        |""".stripMargin,
        "descriptor_stream.c"
      )
      .moreCode(
        """
        |#include <fcntl.h>
        |#include <string.h>
        |#include <sys/socket.h>
        |#include <arpa/inet.h>
        |#include <netinet/in.h>
        |#include <unistd.h>
        |
        |/* the fixture's own prototype hands a descriptor to the stream release,
        |   whose real parameter is a FILE *: the fd family released by the file
        |   family's call */
        |int fclose(int stream);
        |
        |/* a socket is a descriptor of the same family open returns, so close
        |   releases it: neither the release after the failed connect nor the one
        |   at the end is a finding */
        |int socket_connect_close(const char *host, unsigned short port)
        |{
        |    int fd = socket(AF_INET, SOCK_STREAM, 0);
        |    struct sockaddr_in addr;
        |    if (fd < 0)
        |        return -1;
        |    memset(&addr, 0, sizeof(addr));
        |    addr.sin_family = AF_INET;
        |    addr.sin_port = htons(port);
        |    addr.sin_addr.s_addr = inet_addr(host);
        |    if (connect(fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        |        close(fd);
        |        return -1;
        |    }
        |    close(fd);
        |    return 0;
        |}
        |
        |/* an open descriptor released with close: the fd family's own release */
        |int open_close(const char *path)
        |{
        |    int fd = open(path, O_RDONLY);
        |    if (fd < 0)
        |        return -1;
        |    close(fd);
        |    return 0;
        |}
        |
        |/* the descriptor handed to the stream release */
        |int descriptor_closed_with_fclose(const char *path)
        |{
        |    int fd = open(path, O_RDONLY);
        |    if (fd < 0)
        |        return -1;
        |    fclose(fd);
        |    return 0;
        |}
        |
        |/* close and close again: the second close releases a descriptor the
        |   first already released */
        |int double_close_fd(const char *path)
        |{
        |    int fd = open(path, O_RDONLY);
        |    if (fd < 0)
        |        return -1;
        |    close(fd);
        |    close(fd);
        |    return 0;
        |}
        |""".stripMargin,
        "descriptor_fd.c"
      )

  new MemoryApiPass(cpg).createAndApply()
  new ExtentPass(cpg).createAndApply()
  new GuardPass(cpg).createAndApply()
  new ValueOriginPass(cpg).createAndApply()
  new IntegerWidthPass(cpg).createAndApply()
  new AllocationStatePass(cpg).createAndApply()
  new MemorySafetyFindingPass(cpg).createAndApply()
  // The pipeline writes this alongside the overlay (atom#397); without it the command refuses to
  // render a C graph, since missing tags and a finding-free program look alike.
  Atom.storeMemorySafetyOverlayMarker(cpg)

  private def render(config: AtomMemorySafetyConfig): Either[String, List[io.circe.Json]] =
    val out = File.newTemporaryFile("memory-safety", ".json")
    out.deleteOnExit()
    config.outputSliceFile = out
    MemorySafetyCommands.runMemorySafety(cpg, config, out).map { _ =>
        parse(out.contentAsString).toOption.flatMap(_.asArray.map(_.toList)).getOrElse(Nil)
    }

  private def findingOf(rule: String): io.circe.Json =
      render(AtomMemorySafetyConfig()).toOption
          .getOrElse(Nil)
          .find(_.hcursor.get[String]("rule").toOption.contains(rule))
          .getOrElse(fail(s"no $rule finding was rendered"))

  private def findingAt(rule: String, file: String, line: Int, floor: String): io.circe.Json =
      render(AtomMemorySafetyConfig().withMinConfidence(floor)).toOption
          .getOrElse(Nil)
          .find { f =>
              f.hcursor.get[String]("rule").toOption.contains(rule) &&
              f.hcursor.get[String]("file").toOption.exists(_.endsWith(file)) &&
              f.hcursor.get[Int]("line").toOption.contains(line)
          }
          .getOrElse(fail(s"no $rule finding at $file:$line was rendered"))

  private def linesOf(rule: String, file: String, floor: String): Set[Int] =
      render(AtomMemorySafetyConfig().withMinConfidence(floor)).toOption
          .getOrElse(Nil)
          .filter(_.hcursor.get[String]("rule").toOption.contains(rule))
          .filter(_.hcursor.get[String]("file").toOption.exists(_.endsWith(file)))
          .flatMap(_.hcursor.get[Int]("line").toOption)
          .toSet

  private def tuplesIn(file: String, floor: String): Set[(String, Int, String)] =
      render(AtomMemorySafetyConfig().withMinConfidence(floor)).toOption
          .getOrElse(Nil)
          .filter(_.hcursor.get[String]("file").toOption.exists(_.endsWith(file)))
          .flatMap { f =>
              for
                rule       <- f.hcursor.get[String]("rule").toOption
                line       <- f.hcursor.get[Int]("line").toOption
                confidence <- f.hcursor.get[String]("confidence").toOption
              yield (rule, line, confidence)
          }
          .toSet

  /** The GNU cleanup attribute end to end, run through the command the way a user does, because the
    * comparison the fixture exists for is between the two C front ends and both halves must travel
    * the same path.
    */
  private val cleanupSource =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |/* The GNU cleanup attribute on an automatic variable calls the named
        |   function on it at every exit of its scope: before each return and where
        |   the block ends. */
        |
        |static inline void freep(void *p) { free(*(void **)p); }
        |
        |/* the block is released at scope exit: nothing leaks */
        |int cleanup_ok(const char *data, size_t len)
        |{
        |    char *buf __attribute__((cleanup(freep))) = (char *)malloc(len + 1);
        |    if (buf == NULL)
        |        return -1;
        |    memcpy(buf, data, len);
        |    buf[len] = 0;
        |    return buf[0];
        |}
        |
        |/* freed here and again at scope exit */
        |int cleanup_double_free(size_t len)
        |{
        |    char *buf __attribute__((cleanup(freep))) = (char *)malloc(len);
        |    int v;
        |    if (buf == NULL)
        |        return -1;
        |    memset(buf, 0, len);
        |    v = (int)buf[0];
        |    free(buf);
        |    return v;
        |}
        |
        |/* handed to the caller, then released at scope exit: the caller holds a
        |   dangling pointer */
        |char *cleanup_return_dangling(size_t len)
        |{
        |    char *buf __attribute__((cleanup(freep))) = (char *)malloc(len);
        |    if (buf == NULL)
        |        return NULL;
        |    memset(buf, 0, len);
        |    return buf;
        |}
        |
        |/* ownership transferred out before the scope ends: the cleanup call frees
        |   the NULL the steal left */
        |static inline void *steal(void **pp)
        |{
        |    void *p = *pp;
        |    *pp = NULL;
        |    return p;
        |}
        |
        |char *cleanup_return_stolen(size_t len)
        |{
        |    char *buf __attribute__((cleanup(freep))) = (char *)malloc(len);
        |    if (buf == NULL)
        |        return NULL;
        |    memset(buf, 0, len);
        |    return (char *)steal((void **)&buf);
        |}
        |""".stripMargin

  /** A callee that returns its own cleanup variable: the scope's release runs on the way out, so
    * the block the caller receives is already freed and the caller's write through it is the use
    * after free.
    */
  private val cleanupReturnUseSource =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |static inline void freep(void *p) { free(*(void **)p); }
        |
        |/* returns a block the scope's own cleanup call releases on the way out:
        |   the caller receives a dangling pointer */
        |static char *make_then_cleanup_frees(size_t len)
        |{
        |    char *buf __attribute__((cleanup(freep))) = (char *)malloc(len);
        |    if (buf == NULL)
        |        return NULL;
        |    memset(buf, 0, len);
        |    return buf;
        |}
        |
        |/* writes through the returned block after the callee's cleanup released it */
        |int use_after_cleanup_returned(size_t len)
        |{
        |    char *p = make_then_cleanup_frees(len);
        |    if (p == NULL)
        |        return -1;
        |    p[0] = 'x';
        |    return p[0];
        |}
        |""".stripMargin

  /** Ownership handed out through a steal helper before the scope ends: the stolen pointer owns the
    * block and frees it, and the scope's cleanup call then releases the NULL the steal left behind.
    */
  private val cleanupStealSource =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |static inline void freep(void *p) { free(*(void **)p); }
        |
        |/* the g_steal_pointer shape: reads the pointee, stores NULL behind it and
        |   returns the value it read */
        |static inline void *steal_pointer(void **pp)
        |{
        |    void *p = *pp;
        |    *pp = NULL;
        |    return p;
        |}
        |
        |/* the block leaves through the stolen pointer, which frees it; the
        |   scope's cleanup call then releases the NULL the steal left */
        |int steal_then_scope_ends(size_t len)
        |{
        |    char *buf __attribute__((cleanup(freep))) = (char *)malloc(len);
        |    char *owned;
        |    int v;
        |    if (buf == NULL)
        |        return -1;
        |    memset(buf, 0, len);
        |    owned = (char *)steal_pointer((void **)&buf);
        |    v = owned[0];
        |    free(owned);
        |    return v;
        |}
        |""".stripMargin

  /** The free-and-reset idiom inside a cleanup scope: the hand free empties the variable, so the
    * scope's cleanup call releases the NULL left behind.
    */
  private val cleanupResetSource =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |static inline void freep(void *p) { free(*(void **)p); }
        |
        |/* freed by hand and reset to NULL: the scope's cleanup call releases the
        |   NULL the reset left */
        |int free_reset_then_scope_ends(size_t len)
        |{
        |    char *buf __attribute__((cleanup(freep))) = (char *)malloc(len);
        |    int v;
        |    if (buf == NULL)
        |        return -1;
        |    memset(buf, 0, len);
        |    v = (int)buf[0];
        |    free(buf);
        |    buf = NULL;
        |    return v;
        |}
        |""".stripMargin

  /** A cleanup variable stored into storage that outlives the scope: the store hands out a block
    * the scope's cleanup call is about to release.
    */
  private val cleanupGlobalSource =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |static inline void freep(void *p) { free(*(void **)p); }
        |
        |static char *eternal;
        |
        |/* the block outlives the scope through the global, but the scope's
        |   cleanup call still releases it: the store hands out freed memory */
        |void store_cleanup_block_in_global(size_t len)
        |{
        |    char *buf __attribute__((cleanup(freep))) = (char *)malloc(len);
        |    if (buf == NULL)
        |        return;
        |    memset(buf, 0, len);
        |    eternal = buf;
        |}
        |""".stripMargin

  /** Every cleanup-attribute fixture: the shared one plus the shapes the scope-release rules own,
    * each compared across both C front ends.
    */
  private val cleanupFixtures: List[(String, String)] = List(
    ("cleanup.c", cleanupSource),
    ("cleanup_return_use.c", cleanupReturnUseSource),
    ("cleanup_steal_scope.c", cleanupStealSource),
    ("cleanup_reset.c", cleanupResetSource),
    ("cleanup_global.c", cleanupGlobalSource)
  )

  /** The cleanup fixture's findings as (rule, line, confidence) through one front end of the
    * command, at the lowest floor so nothing is hidden.
    */
  private def cleanupFindings(
    frontend: String,
    dir: File,
    floor: String = "low"
  ): Set[(String, Int, String)] =
    val findings = dir / s"findings-$frontend.json"
    Atom.run(
      Array(
        "memory-safety",
        "-l",
        "c",
        "--c-frontend",
        frontend,
        "--no-ast-cache",
        "--min-confidence",
        floor,
        "-o",
        (dir / s"$frontend.atom").pathAsString,
        "-s",
        findings.pathAsString,
        (dir / "src").pathAsString
      )
    ) match
      case Left(error) => fail(s"the $frontend front end run failed: $error")
      case Right(_) =>
          parse(findings.contentAsString).toOption.flatMap(_.asArray).toList.flatten
              .flatMap { f =>
                  for
                    rule       <- f.hcursor.get[String]("rule").toOption
                    line       <- f.hcursor.get[Int]("line").toOption
                    confidence <- f.hcursor.get[String]("confidence").toOption
                  yield (rule, line, confidence)
              }.toSet
    end match
  end cleanupFindings

  /** The bump pool whose allocations belong to the pool, not to the heap: `pool_alloc` hands out
    * `p->base + p->used`, pointer arithmetic over a field of its pool parameter, so its result dies
    * with the pool's destroy and the heap's `free` has no business releasing it.
    */
  private val poolProbeSource =
      """#include <stdlib.h>
        |#include <stddef.h>
        |
        |typedef struct pool { char *base; size_t used, cap; } pool_t;
        |
        |pool_t *pool_create(size_t cap)
        |{
        |    pool_t *p = (pool_t *)malloc(sizeof(*p));
        |    if (!p) return NULL;
        |    p->base = (char *)malloc(cap);
        |    p->used = 0;
        |    p->cap = cap;
        |    return p;
        |}
        |
        |void *pool_alloc(pool_t *p, size_t n)
        |{
        |    if (p->cap - p->used < n) return NULL;
        |    void *r = p->base + p->used;
        |    p->used += n;
        |    return r;
        |}
        |
        |void pool_destroy(pool_t *p) { free(p->base); free(p); }
        |
        |/* the block belongs to the pool, so nothing leaks and no heap free is due */
        |int pool_ok(pool_t *p, size_t n)
        |{
        |    char *s = (char *)pool_alloc(p, n);
        |    if (!s) return -1;
        |    s[0] = 1;
        |    return 0;
        |}
        |
        |/* free() of pool memory: the block the pool owns is not the heap's */
        |int pool_free_mismatch(pool_t *p, size_t n)
        |{
        |    char *s = (char *)pool_alloc(p, n);
        |    free(s);
        |    return 0;
        |}
        |
        |/* a use of pool memory the destroy has already torn down */
        |int pool_uaf(size_t n)
        |{
        |    pool_t *p = pool_create(4096);
        |    if (!p) return -1;
        |    char *s = (char *)pool_alloc(p, n);
        |    pool_destroy(p);
        |    return s ? s[0] : 0;
        |}
        |""".stripMargin

  /** The declared-region fixture's header, written beside the source so the unit includes it and
    * the profile selection has a header basename to match. Declarations only, so no body can be
    * inferred and the profile entries are what govern the calls.
    */
  private val declaredRegionHeader =
      """#ifndef PGMEM_H
        |#define PGMEM_H
        |#include <stddef.h>
        |void *palloc(size_t size);
        |void pfree(void *pointer);
        |void ctx_reset(void *context);
        |void ctx_destroy(void *context);
        |extern void *CurrentMemoryContext;
        |#endif
        |""".stripMargin

  /** The declared-region fixture's config, passed through `--memory-api-config`. The `profiles`
    * section carries the region roles a unit selects by including `pgmem.h`: `palloc` draws from
    * the `CurrentMemoryContext` global, `ctx_reset` empties the context named by its argument,
    * `ctx_destroy` tears it down. The `apis` section names `pfree`'s release family for the whole
    * input, which keeps the name-and-signature heuristic from guessing one for it.
    */
  private val regionApiConfig =
      """{
        |  "apis": [
        |    {"name": "pfree", "free": "pgmem",
        |     "source": "utils/palloc.h: pfree frees memory allocated with palloc"}
        |  ],
        |  "profiles": [
        |    {
        |      "name": "testpg",
        |      "headers": ["pgmem.h"],
        |      "entries": [
        |        {"name": "palloc", "len": 1, "alloc": "pgmem", "nullableReturn": true,
        |         "region": {"role": "allocate", "context": "CurrentMemoryContext"},
        |         "source": "utils/palloc.h: palloc allocates from the current memory context"},
        |        {"name": "pfree", "free": "pgmem",
        |         "source": "utils/palloc.h: pfree frees memory allocated with palloc"},
        |        {"name": "ctx_reset", "region": {"role": "reset", "arg": 1},
        |         "source": "utils/memutils.h: reset frees the context's allocations, the context stays valid"},
        |        {"name": "ctx_destroy", "region": {"role": "destroy", "arg": 1},
        |         "source": "utils/memutils.h: delete frees the context's memory and the context itself"}
        |      ]
        |    }
        |  ]
        |}
        |""".stripMargin

  /** The declared-region fixture: a matched palloc/pfree pair, a pfree of malloc memory, and a use
    * of context memory after a reset and after a destroy.
    */
  private val declaredRegionSource =
      """#include <stdlib.h>
        |#include <stddef.h>
        |#include "pgmem.h"
        |
        |/* the matched pair: the release the allocation's own family names */
        |int pg_ok(size_t n)
        |{
        |    char *p = (char *)palloc(n);
        |    if (!p) return -1;
        |    p[0] = 1;
        |    pfree(p);
        |    return 0;
        |}
        |
        |/* pfree of a block malloc produced: the families do not meet */
        |int pg_pfree_of_malloc(size_t n)
        |{
        |    char *q = (char *)malloc(n);
        |    if (!q) return -1;
        |    pfree(q);
        |    return 0;
        |}
        |
        |/* the reset empties the context and leaves the context itself valid */
        |int pg_reset_then_use(size_t n)
        |{
        |    char *p = (char *)palloc(n);
        |    if (!p) return -1;
        |    ctx_reset(CurrentMemoryContext);
        |    return p[0];
        |}
        |
        |/* the destroy tears the context and every block it handed out down */
        |int pg_destroy_then_use(size_t n)
        |{
        |    char *p = (char *)palloc(n);
        |    if (!p) return -1;
        |    ctx_destroy(CurrentMemoryContext);
        |    return p[0];
        |}
        |""".stripMargin

  /** The region-full fallback: the bump pool is full, so the allocator hands back a real allocation
    * and the caller releases that arm by hand before the destroy takes the pool's own blocks away.
    * Neither arm leaks: the region arm dies with the destroy, the fallback arm with the free.
    */
  private val poolFallbackSource =
      """#include <stdlib.h>
        |#include <stddef.h>
        |
        |typedef struct pool { char *base; size_t used, cap; } pool_t;
        |
        |pool_t *pool_create(size_t cap)
        |{
        |    pool_t *p = (pool_t *)malloc(sizeof(*p));
        |    if (!p) return NULL;
        |    p->base = (char *)malloc(cap);
        |    p->used = 0;
        |    p->cap = cap;
        |    return p;
        |}
        |
        |void pool_destroy(pool_t *p) { free(p->base); free(p); }
        |
        |/* the region-full case falls back to a real allocation */
        |char *pool_alloc_or_heap(pool_t *p, size_t n)
        |{
        |    if (p->cap - p->used < n)
        |        return (char *)malloc(n);
        |    void *r = p->base + p->used;
        |    p->used += n;
        |    return r;
        |}
        |
        |void fallback_ok(size_t n)
        |{
        |    pool_t *p = pool_create(16);
        |    char *s;
        |    if (!p) return;
        |    s = pool_alloc_or_heap(p, n);
        |    if (!s) { pool_destroy(p); return; }
        |    s[0] = 1;
        |    if (n > 16)
        |        free(s);
        |    pool_destroy(p);
        |}
        |""".stripMargin

  /** The reference-counted triple the probe carries: a constructor whose body seeds the count, an
    * acquire that increments it and a release that decrements it and frees the object when it
    * reaches zero. The read after the release that spent the object's own reference is the use
    * after free, the release after the spent one is the double release, and the balanced pair, the
    * count store this frame wrote and the spent release stay silent.
    */
  private val refcountSource =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |struct obj { int refs; char *name; };
        |
        |struct obj *obj_new(const char *name)
        |{
        |    struct obj *o = (struct obj *)calloc(1, sizeof(*o));
        |    if (o == NULL) return NULL;
        |    o->refs = 1;
        |    o->name = strdup(name);
        |    return o;
        |}
        |
        |void obj_ref(struct obj *o) { o->refs++; }
        |
        |void obj_unref(struct obj *o)
        |{
        |    if (--o->refs == 0) {
        |        free(o->name);
        |        free(o);
        |    }
        |}
        |
        |/* the last reference is dropped, then the object is read */
        |int refcount_uaf(void)
        |{
        |    struct obj *o = obj_new("session");
        |    if (o == NULL) return -1;
        |    obj_unref(o);
        |    return o->refs;
        |}
        |
        |/* the count goes below zero: the second release frees the freed object again */
        |void refcount_double_unref(void)
        |{
        |    struct obj *o = obj_new("once");
        |    if (o == NULL) return;
        |    obj_unref(o);
        |    obj_unref(o);
        |}
        |
        |/* the acquire and the release balance on a borrowed parameter: the acquired
        |   reference is the one the release drops, and the caller's own reference
        |   keeps the object alive */
        |int balanced_pair_ok(struct obj *o)
        |{
        |    obj_ref(o);
        |    obj_unref(o);
        |    return o->refs;
        |}
        |
        |/* the count this frame wrote (two) keeps the object past the release */
        |int count_store_ok(void)
        |{
        |    struct obj *o = obj_new("shared");
        |    int r;
        |    if (o == NULL) return -1;
        |    o->refs = 2;
        |    obj_unref(o);
        |    r = o->refs;
        |    obj_unref(o);
        |    return r;
        |}
        |
        |/* the last reference is spent and nothing reads the object after */
        |int release_only_ok(void)
        |{
        |    struct obj *o = obj_new("spent");
        |    if (o == NULL) return -1;
        |    obj_unref(o);
        |    return 0;
        |}
        |""".stripMargin

  /** The borrowed release: the object arrives as a parameter, so the release drops a reference
    * whose ownership the method cannot account for, and the object is only maybe gone. The use
    * after it is the weakest claim, and the same release with no later use stays silent.
    */
  private val borrowedSource =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |struct obj { int refs; char *name; };
        |
        |struct obj *obj_new(const char *name)
        |{
        |    struct obj *o = (struct obj *)calloc(1, sizeof(*o));
        |    if (o == NULL) return NULL;
        |    o->refs = 1;
        |    o->name = strdup(name);
        |    return o;
        |}
        |
        |void obj_ref(struct obj *o) { o->refs++; }
        |
        |void obj_unref(struct obj *o)
        |{
        |    if (--o->refs == 0) {
        |        free(o->name);
        |        free(o);
        |    }
        |}
        |
        |/* the borrowed release: the object came in as a parameter, so the release
        |   drops a reference whose ownership the method cannot account for */
        |int borrowed_use(struct obj *o)
        |{
        |    obj_unref(o);
        |    return o->refs;
        |}
        |
        |/* the borrowed release with nothing after it */
        |int borrowed_release_only(struct obj *o)
        |{
        |    obj_unref(o);
        |    return 0;
        |}
        |""".stripMargin

  /** The declared-route fixture's header, written beside the source so the unit includes it and the
    * profile selection has a header basename to match. Declarations only, so no body can be
    * inferred and the profile entries are what govern the calls.
    */
  private val declaredRefHeader =
      """#ifndef REFOPS_H
        |#define REFOPS_H
        |
        |struct obj { int refs; char *name; };
        |
        |void ref_get(struct obj *o);
        |void ref_put(struct obj *o);
        |
        |#endif
        |""".stripMargin

  /** The declared-route fixture's config, passed through `--memory-api-config`. The profile carries
    * the reference roles a unit selects by including `refops.h`: `ref_put` drops the reference of
    * its argument (the release role's value is the parameter index) and `ref_get` takes one. The
    * `apis` section stays empty: the two names live outside the analysed tree, so nothing in it can
    * carry a family or a size role.
    */
  private val declaredRefConfig =
      """{
        |  "apis": [],
        |  "profiles": [
        |    {
        |      "name": "testref",
        |      "headers": ["refops.h"],
        |      "entries": [
        |        {"name": "ref_get", "acquire": 1,
        |         "source": "refops.h: ref_get takes one reference of the object"},
        |        {"name": "ref_put", "release": 1,
        |         "source": "refops.h: ref_put drops one reference, freeing the object when the count reaches zero"}
        |      ]
        |    }
        |  ]
        |}
        |""".stripMargin

  /** The declared-route fixture's source: the same defect and silence shapes the inferred release
    * carries, driven by calloc's own allocation and the profile's declared roles.
    */
  private val declaredRefSource =
      """#include <stdlib.h>
        |#include <string.h>
        |#include "refops.h"
        |
        |/* the last reference is dropped, then the object is read */
        |int decl_uaf(void)
        |{
        |    struct obj *o = (struct obj *)calloc(1, sizeof(*o));
        |    if (o == NULL) return -1;
        |    ref_put(o);
        |    return o->refs;
        |}
        |
        |/* the count goes below zero: the second release is the double release */
        |void decl_double_unref(void)
        |{
        |    struct obj *o = (struct obj *)calloc(1, sizeof(*o));
        |    if (o == NULL) return;
        |    ref_put(o);
        |    ref_put(o);
        |}
        |
        |/* the count this frame wrote (two) keeps the object past the release */
        |int decl_count_store_ok(void)
        |{
        |    struct obj *o = (struct obj *)calloc(1, sizeof(*o));
        |    int r;
        |    if (o == NULL) return -1;
        |    o->refs = 2;
        |    ref_put(o);
        |    r = o->refs;
        |    ref_put(o);
        |    return r;
        |}
        |
        |/* the acquire and the release balance on a borrowed parameter */
        |int decl_balanced_ok(struct obj *o)
        |{
        |    ref_get(o);
        |    ref_put(o);
        |    return o->refs;
        |}
        |
        |/* the release with nothing after it */
        |void decl_release_only(void)
        |{
        |    struct obj *o = (struct obj *)calloc(1, sizeof(*o));
        |    if (o == NULL) return;
        |    ref_put(o);
        |}
        |""".stripMargin

  /** The release's negative controls: every element of the loop drops its reference in its own
    * iteration, and the error path releases before the jump with nothing after it.
    */
  private val refControlsSource =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |struct obj { int refs; char *name; };
        |
        |struct obj *obj_new(const char *name)
        |{
        |    struct obj *o = (struct obj *)calloc(1, sizeof(*o));
        |    if (o == NULL) return NULL;
        |    o->refs = 1;
        |    o->name = strdup(name);
        |    return o;
        |}
        |
        |void obj_ref(struct obj *o) { o->refs++; }
        |
        |void obj_unref(struct obj *o)
        |{
        |    if (--o->refs == 0) {
        |        free(o->name);
        |        free(o);
        |    }
        |}
        |
        |/* a release in a loop body over a list: every element drops its reference in
        |   its own iteration, and the next iteration's element is a different one */
        |struct node { struct obj *obj; struct node *next; };
        |
        |void release_each_in_its_iteration(struct node *head)
        |{
        |    struct node *n;
        |    for (n = head; n != NULL; n = n->next)
        |        obj_unref(n->obj);
        |}
        |
        |/* the release on the goto error path: the error path uses nothing after it */
        |int goto_release_nothing_after(struct obj *o, int mode)
        |{
        |    int rc = 0;
        |    if (mode) {
        |        obj_unref(o);
        |        rc = -1;
        |        goto out;
        |    }
        |    rc = o->refs;
        |out:
        |    return rc;
        |}
        |""".stripMargin

  /** A region fixture's findings as (rule, line, confidence) through one front end of the command,
    * at the requested floor, with any extra command options a fixture needs (the declared one
    * passes its config through `--memory-api-config`).
    */
  private def regionFindings(
    frontend: String,
    dir: File,
    floor: String = "low",
    extra: String*
  ): Set[(String, Int, String)] =
    val findings = dir / s"findings-$frontend.json"
    Atom.run(
      Array(
        "memory-safety",
        "-l",
        "c",
        "--c-frontend",
        frontend,
        "--no-ast-cache",
        "--min-confidence",
        floor,
        "-o",
        (dir / s"$frontend.atom").pathAsString,
        "-s",
        findings.pathAsString
      ) ++ extra ++ Array((dir / "src").pathAsString)
    ) match
      case Left(error) => fail(s"the $frontend front end run failed: $error")
      case Right(_) =>
          parse(findings.contentAsString).toOption.flatMap(_.asArray).toList.flatten
              .flatMap { f =>
                  for
                    rule       <- f.hcursor.get[String]("rule").toOption
                    line       <- f.hcursor.get[Int]("line").toOption
                    confidence <- f.hcursor.get[String]("confidence").toOption
                  yield (rule, line, confidence)
              }.toSet
    end match
  end regionFindings

  /** Captures the console, so a run's profile selection line has to say what it did. Scala's
    * `println` goes through `scala.Console`, which caches its stream, so both it and `System.out`
    * are redirected.
    */
  private def captureConsole[A](body: => A): String =
    val original = System.out
    val captured = new ByteArrayOutputStream()
    System.setOut(new PrintStream(captured, true, "UTF-8"))
    try
      scala.Console.withOut(System.out)(body)
      captured.toString("UTF-8")
    finally System.setOut(original)

  "the memory-safety command" should {

      "render the finding contract a scorer reads" in {
          val f = findingOf(MemorySafetyFindingPass.RuleSizeParamContract).hcursor
          f.get[String]("rule").toOption shouldBe Some(
            MemorySafetyFindingPass.RuleSizeParamContract
          )
          f.get[String]("cwe").toOption shouldBe Some("CWE-787")
          f.get[String]("kind").toOption shouldBe Some("size-param-contract-violation")
          f.get[String]("confidence").toOption shouldBe Some("high")
          f.get[String]("file").toOption.exists(_.endsWith("copy.c")) shouldBe true
          f.get[Int]("line").toOption.exists(_ > 0) shouldBe true
      }

      "carry the evidence the rule read, one row per fact" in {
          val flow = findingOf(MemorySafetyFindingPass.RuleSizeParamContract).hcursor
              .downField("flow")
              .as[List[Map[String, io.circe.Json]]]
              .toOption
              .getOrElse(Nil)
          val codes = flow.flatMap(_.get("code").flatMap(_.asString))

          codes.exists(_.startsWith("origin: ")) shouldBe true
          // one row per origin, not one per character of it
          codes.count(_.startsWith("origin: ")) shouldBe 1
          codes.exists(_.contains("destination extent param:size")) shouldBe true
          flow.flatMap(_.get("role").flatMap(_.asString)) should contain("memory-operation")
      }

      "reject a format or confidence it does not implement rather than ignoring it" in {
          render(AtomMemorySafetyConfig().withFormat("xml")).isLeft shouldBe true
          render(AtomMemorySafetyConfig().withMinConfidence("very-high")).isLeft shouldBe true
          render(AtomMemorySafetyConfig().withMinConfidence("HIGH")).isRight shouldBe true
      }

      "write SARIF 2.1.0 the official schema accepts, with rules, locations and code flows" in {
          val out = File.newTemporaryFile("memory-safety", ".sarif")
          out.deleteOnExit()
          val config = AtomMemorySafetyConfig().withFormat("sarif")
          config.outputSliceFile = out
          MemorySafetyCommands.runMemorySafety(cpg, config, out).isRight shouldBe true
          val text = out.contentAsString
          val factory = com.networknt.schema.JsonSchemaFactory.getInstance(
            com.networknt.schema.SpecVersion.VersionFlag.V4
          )
          val schema = factory.getSchema(
            getClass.getResourceAsStream("/sarif/sarif-schema-2.1.0.json")
          )
          val mapper = new com.fasterxml.jackson.databind.ObjectMapper()
          val errors = schema.validate(mapper.readTree(text))
          errors.isEmpty shouldBe true
          // and the schema does reject what is not SARIF: a log without a version
          schema.validate(mapper.readTree("""{"runs": []}""")).isEmpty shouldBe false

          val sarif = parse(text).toOption.get
          val run   = sarif.hcursor.downField("runs").downArray
          val rules = run.downField("tool").downField("driver").downField("rules").as[List[
            io.circe.Json
          ]].toOption.get
          val results = run.downField("results").as[List[io.circe.Json]].toOption.get
          val json    = render(AtomMemorySafetyConfig()).toOption.get
          results.size shouldBe json.size
          val ruleIds = rules.flatMap(_.hcursor.get[String]("id").toOption)
          ruleIds should contain(MemorySafetyFindingPass.RuleSizeParamContract)
          // each result names its rule by id and index, and carries the CWE
          results.foreach { r =>
            val id = r.hcursor.get[String]("ruleId").toOption.get
            r.hcursor.get[Int]("ruleIndex").toOption shouldBe Some(ruleIds.indexOf(id))
            r.hcursor.downField("properties").get[String]("cwe").toOption.get should startWith(
              "CWE-"
            )
          }
          val sizeParam = results.find(
            _.hcursor.get[String]("ruleId").toOption.contains(
              MemorySafetyFindingPass.RuleSizeParamContract
            )
          ).get
          val location =
              sizeParam.hcursor.downField("locations").downArray.downField("physicalLocation")
          location.downField("artifactLocation").get[String]("uri").toOption.get should endWith(
            "copy.c"
          )
          location.downField("region").get[Int]("startLine").toOption.get should be > 0
          sizeParam.hcursor.downField("codeFlows").downArray.downField("threadFlows").downArray
              .downField("locations").as[List[io.circe.Json]].toOption.get should not be empty
      }

      "index SARIF results into the rules listed, and keep a file outside the input absolute" in {
          val known = MemorySafetyFindingPass.RuleSizeParamContract
          def finding(rule: String, file: String) = io.circe.Json.obj(
            "rule"     -> io.circe.Json.fromString(rule),
            "file"     -> io.circe.Json.fromString(file),
            "line"     -> io.circe.Json.fromInt(3),
            "severity" -> io.circe.Json.fromString("high")
          )
          // "MS-AAA-000" sorts before every registered id and is not one. The outside-the-input
          // file is written as the platform would write it: a POSIX absolute path is not absolute
          // on Windows, where `Paths.get("/usr/...")` is a current-drive-relative path instead.
          val outsideHeader =
              if scala.util.Properties.isWin then "C:\\Windows\\include\\string.h"
              else "/usr/include/string.h"
          val sarif = MemorySafetySarif.document(
            List(finding("MS-AAA-000", "src/a.c"), finding(known, outsideHeader)),
            "/work/project",
            None
          )
          val run = sarif.hcursor.downField("runs").downArray
          val ruleIds = run.downField("tool").downField("driver").downField("rules")
              .as[List[io.circe.Json]].toOption.get.flatMap(_.hcursor.get[String]("id").toOption)
          ruleIds shouldBe List(known)
          val results = run.downField("results").as[List[io.circe.Json]].toOption.get
          results.map(_.hcursor.get[Int]("ruleIndex").toOption.get) shouldBe List(-1, 0)
          def artifact(r: io.circe.Json) = r.hcursor.downField("locations").downArray
              .downField("physicalLocation").downField("artifactLocation")
          artifact(results.head).get[String]("uri").toOption shouldBe Some("src/a.c")
          artifact(results.head).get[String]("uriBaseId").toOption shouldBe Some("%SRCROOT%")
          artifact(results.last).get[String]("uri").toOption.get should startWith("file:/")
          artifact(results.last).get[String]("uri").toOption.get should endWith(
            "string.h"
          )
          artifact(results.last).get[String]("uriBaseId").toOption shouldBe None
      }

      "keep the flags set before it: a command option must not reset the config" in {
          // AtomConfig holds language/dataDeps/output paths in the TRAIT's vars, so a `copy`-based
          // option silently returns them to their defaults. `atom memory-safety -l c
          // --min-confidence medium` lost the `-l` and died with "No language frontend supported
          // for language ''" - invisible to any test that builds the config directly.
          val config = AtomMemorySafetyConfig()
          config.withLanguage("c").withDataDependencies(true)
          config.withMinConfidence("medium").withFormat("json")
          config.language shouldBe "c"
          config.dataDeps shouldBe true
      }

      "say none and tag nothing when --memory-api-profiles none turns profiles off" in {
          // the unit includes glib's own header, so automatic selection would apply the glib
          // profile; the option turns that off, and the printed selection has to say so
          val profileCpg = new DataFlowTestCpg()
              .moreCode(
                "void *g_new0(unsigned long type_size, unsigned long n_structs);\n",
                "gmem.h"
              )
              .moreCode(
                """
                  |#include "gmem.h"
                  |
                  |void *spend_glib(unsigned long n)
                  |{
                  |    return g_new0(1, n);
                  |}
                  |""".stripMargin,
                "glib_user.c"
              )
          val printed = captureConsole {
              new MemoryApiPass(profileCpg, None, MemoryApiPass.ProfilesNone, reportProfiles = true)
                  .createAndApply()
          }
          printed should include("MemoryApiPass: memory-api profiles: none")
          profileCpg.call.name("g_new0").tag.name.l should not contain "mem-alloc"
      }

      "say and apply a forced --memory-api-profiles name, whatever the unit includes" in {
          // the unit includes none of the profile's headers, so automatic selection would choose
          // nothing; the forced name governs the file's calls anyway, and the printed selection
          // names the profile with the number of files that follow it
          val plainCpg = new DataFlowTestCpg()
              .moreCode(
                """
                  |void *g_new0(unsigned long type_size, unsigned long n_structs);
                  |
                  |void *spend_glib(unsigned long n)
                  |{
                  |    return g_new0(1, n);
                  |}
                  |""".stripMargin,
                "plain_user.c"
              )
          val printed = captureConsole {
              new MemoryApiPass(plainCpg, None, "glib", reportProfiles = true).createAndApply()
          }
          printed should include("MemoryApiPass: memory-api profiles: glib (1 file)")
          plainCpg.call.name("g_new0").tag.name("mem-alloc").value.l shouldBe List("heap")
      }

      "render an allocation-state finding" in {
          // MS-ALLOC-003 is the first rule whose findings do not sit on a length argument, and
          // the renderer dropped every one of them: it matched `Expression | ControlStructure`,
          // which is just Expression, while an exit-anchored finding can be a METHOD_RETURN
          val f = findingOf(MemorySafetyFindingPass.RuleLeak).hcursor
          f.get[String]("cwe").toOption shouldBe Some("CWE-401")
          f.get[String]("file").toOption.exists(_.endsWith("alloc.c")) shouldBe true
          f.get[Int]("line").toOption.exists(_ > 0) shouldBe true
      }

      "render a leak anchored at a bare `return;` (an exit node is an anchor too)" in {
          // the finding's node IS the RETURN - not the allocation, not an argument
          val f = findingOf(MemorySafetyFindingPass.RuleLeak).hcursor
          f.get[String]("file").toOption.exists(_.endsWith("alloc.c")) shouldBe true
          // the implicit-end fixture and the bare-return fixture both leak; one of the two
          // rendered findings must sit inside leaks_at_a_bare_return's body, above the free
          val lines = render(AtomMemorySafetyConfig()).toOption
              .getOrElse(Nil)
              .filter(_.hcursor.get[String]("rule").toOption.contains(
                MemorySafetyFindingPass.RuleLeak
              ))
          lines should not be empty
      }

      "render every rule's finding end to end" in {
          // a tag the renderer never surfaces is not a finding; a rule's measured precision is
          // meaningless if the renderer silently drops its findings
          val byRule = render(AtomMemorySafetyConfig()).toOption
              .getOrElse(Nil)
              .flatMap(_.hcursor.get[String]("rule").toOption)
              .toSet
          Seq(
            MemorySafetyFindingPass.RuleSizeParamContract,
            MemorySafetyFindingPass.RuleUnboundedCopy,
            MemorySafetyFindingPass.RuleIndexRead,
            MemorySafetyFindingPass.RuleIndexWrite,
            MemorySafetyFindingPass.RuleResignAcrossGuard,
            MemorySafetyFindingPass.RuleDoubleFree,
            MemorySafetyFindingPass.RuleUseAfterFree,
            MemorySafetyFindingPass.RuleLeak,
            MemorySafetyFindingPass.RuleDoubleClose,
            MemorySafetyFindingPass.RuleNullDeref,
            MemorySafetyFindingPass.RuleStackEscape
          ).foreach(rule => byRule should contain(rule))
      }

      "render the stack-escape finding at its RETURN anchor" in {
          // MS-ESC-001's finding sits on the Return of returns_stack_array: an exit node is
          // exactly where a renderer matching only expressions silently discards findings
          val f = findingOf(MemorySafetyFindingPass.RuleStackEscape).hcursor
          f.get[String]("cwe").toOption shouldBe Some("CWE-562")
          f.get[String]("file").toOption.exists(_.endsWith("escape.c")) shouldBe true
          f.get[Int]("line").toOption.exists(_ > 0) shouldBe true
      }

      "keep the unbraced `if (p) free(p);` silent through the renderer (good_capped)" in {
          // a missed false edge renders this negative control as a leak at the implicit end;
          // the branch narrowing must be visible HERE too, not only in chen's tag assertions
          val leaksInIt = render(AtomMemorySafetyConfig()).toOption
              .getOrElse(Nil)
              .filter(_.hcursor.get[String]("rule").toOption.contains(
                MemorySafetyFindingPass.RuleLeak
              ))
              .filterNot(_.hcursor.get[String]("file").toOption.exists(_.endsWith("escape.c")))
          // the fixtures that legitimately leak are leaks_at_the_implicit_end and
          // leaks_at_a_bare_return; the unbraced one contributes nothing
          leaksInIt.map(_.hcursor.get[String]("file").toOption.getOrElse("")).foreach { f =>
              (f should not).include("unbraced")
          }
      }

      "render MS-NULL-001 only below the medium floor (the per-rule gate demoted it)" in {
          // the per-rule gate: on FFmpeg-style code most of its reports are the idiom of using
          // a nullable result before the error path - so it ships at low, exactly as
          // MS-INT-001 does. What must survive the demotion is the arms' visibility below the
          // floor: the evidence arms and the chained-parameter arm (its own per-finding low).
          def nullDerefLines(floor: String): Set[Int] = render(
            AtomMemorySafetyConfig().withMinConfidence(floor)
          ).toOption
              .getOrElse(Nil)
              .filter(_.hcursor.get[String]("rule").toOption.contains(
                MemorySafetyFindingPass.RuleNullDeref
              ))
              .filter(_.hcursor.get[String]("file").toOption.exists(_.endsWith("null.c")))
              .flatMap(_.hcursor.get[Int]("line").toOption)
              .toSet
          nullDerefLines("medium") shouldBe empty   // the gate, visible end to end
          nullDerefLines("low") should not be empty // both arms render below the floor
      }

      "render a null-deref licensed by a derefs-param summary, not one handed to a non-reader" in {
          // the low arm's null.c findings, by line, with the fixture's three positive shapes
          // (unchecked malloc, chained param, helper summary) and one negative (just_log)
          val lines = render(AtomMemorySafetyConfig().withMinConfidence("low")).toOption
              .getOrElse(Nil)
              .filter(_.hcursor.get[String]("rule").toOption.contains(
                MemorySafetyFindingPass.RuleNullDeref
              ))
              .filter(_.hcursor.get[String]("file").toOption.exists(_.endsWith("null.c")))
              .flatMap(_.hcursor.get[Int]("line").toOption)
              .toSet
          // the three positives sit on three distinct lines; the non-reading helper adds none
          lines should have size 3
          // the just_log call's own line carries no finding: its argument is read by nobody
          val justLogLine = cpg.method
              .name("no_null_deref_via_nonreading_helper")
              .call
              .name("just_log")
              .l
              .headOption
              .flatMap(_.lineNumber.map(_.toInt))
          justLogLine.foreach(l => lines should not contain l)
      }

      "drop findings below the requested confidence" in {
          // the high floor keeps the high-confidence rules (the contract violation, the
          // double free, the resign cast) and drops the medium/low ones entirely
          val high = render(AtomMemorySafetyConfig().withMinConfidence("high")).toOption
              .getOrElse(Nil)
              .flatMap(_.hcursor.get[String]("rule").toOption)
          high should contain(MemorySafetyFindingPass.RuleSizeParamContract)
          high should not contain MemorySafetyFindingPass.RuleLeak
          high should not contain MemorySafetyFindingPass.RuleNullDeref
          high should not contain MemorySafetyFindingPass.RuleStackEscape
      }

      "report a release moved forward off a fresh allocation" in {
          // `free(p + 1)` where p names a block this frame allocated: the
          // release the offset rule exists for
          val rule = MemorySafetyFindingPass.RuleOffsetFree
          val f    = findingAt(rule, "offsetfree.c", 71, "medium").hcursor
          f.get[String]("confidence").toOption shouldBe Some("high")
          f.get[String]("cwe").toOption shouldBe Some("CWE-761")
          f.get[String]("kind").toOption shouldBe Some("free-of-offset-pointer")
      }

      "report a walked-then-freed pointer at the walk's increment" in {
          // the finding sits on the `p++` of the walk, the statement that moved
          // the pointer off the block's start, not on the free
          val rule = MemorySafetyFindingPass.RuleOffsetFree
          val f    = findingAt(rule, "offsetfree.c", 82, "medium").hcursor
          f.get[String]("confidence").toOption shouldBe Some("high")
      }

      "report a release moved back from an allocation advanced by a larger amount" in {
          // `p = p + 4; free(p - 1)`: the pointer is still off the start, so the
          // release is still the rule's, however it got there
          val rule = MemorySafetyFindingPass.RuleOffsetFree
          val f    = findingAt(rule, "offsetfree.c", 93, "medium").hcursor
          f.get[String]("confidence").toOption shouldBe Some("high")
      }

      "stay silent on a header-prefixed allocator's own releases, in every spelling" in {
          // the block starts one header before the pointer the allocator hands
          // out, so moving back onto the header is the pair's own contract: in
          // the call, through a macro's offset, after a decrement or a
          // subtract-assign, and through a local copy of the parameter
          import io.shiftleft.semanticcpg.language.*
          val rule     = MemorySafetyFindingPass.RuleOffsetFree
          val reported = linesOf(rule, "offsetfree.c", "low")
          val contractFrees = cpg.method.name("good_free_.*").call.name("free").l
              .flatMap(_.lineNumber.map(_.toInt))
          contractFrees should not be empty
          contractFrees.foreach(l => reported should not contain l)
      }

      "stay silent on a release after a walk over a caller's pointer" in {
          // the walk started wherever the caller's contract says, so the release
          // after it is that contract's business, not this rule's
          import io.shiftleft.semanticcpg.language.*
          val rule = MemorySafetyFindingPass.RuleOffsetFree
          val freeLine = cpg.method.name("silent_walk_then_free").call.name("free").l
              .headOption.flatMap(_.lineNumber.map(_.toInt))
          freeLine.foreach { l =>
              linesOf(rule, "offsetfree.c", "low") should not contain l
          }
      }

      "report an index the allocation never measured" in {
          // buf[m] into malloc(len + 1): m is not the length the room was
          // sized for
          val rule = MemorySafetyFindingPass.RuleIndexWrite
          val f    = findingAt(rule, "terminator.c", 62, "medium").hcursor
          f.get[String]("confidence").toOption shouldBe Some("medium")
          f.get[String]("cwe").toOption shouldBe Some("CWE-787")
      }

      "report the measured length rewritten between the allocation and the write" in {
          // len += extra rewrites the value the allocation measured, so the
          // write indexes past the room that value bought
          val rule = MemorySafetyFindingPass.RuleIndexWrite
          val f    = findingAt(rule, "terminator.c", 74, "medium").hcursor
          f.get[String]("confidence").toOption shouldBe Some("medium")
      }

      "stay silent on the terminator write the allocation sized room for" in {
          // malloc(len + 1) bounds buf[len]: the write the room was sized for,
          // through a cast of the index, through a copy of the length sizing
          // the allocation, and through a copy of the length indexing the
          // write. Those four shapes and the reported two are the whole file's
          // index-write findings at the lowest floor.
          val rule = MemorySafetyFindingPass.RuleIndexWrite
          linesOf(rule, "terminator.c", "low") shouldBe Set(62, 74)
      }

      "stay silent on the measured length plus the room itself" in {
          // buf[len + 1] into malloc(len + 1): the index arm never engages on a
          // variable-plus-constant index, and the room logic must not disturb
          // that, in either direction
          val rule = MemorySafetyFindingPass.RuleIndexWrite
          linesOf(rule, "terminator.c", "low") should not contain 86
      }

      "report a use after a NULL-guarded destroy function" in {
          // the destroy frees its members and its argument behind a NULL guard
          // on the argument, so whenever the caller handed it a block the call
          // releases it, and the member read after the call reads freed memory
          val rule = MemorySafetyFindingPass.RuleUseAfterFree
          val f    = findingAt(rule, "destroy.c", 51, "medium").hcursor
          f.get[String]("confidence").toOption shouldBe Some("high")
          f.get[String]("kind").toOption shouldBe Some("use-after-free")
          f.get[String]("cwe").toOption shouldBe Some("CWE-416")
      }

      "stay silent on a use after a flag-gated or count-gated release" in {
          // a release another parameter's flag or the object's own reference
          // count decides is not the function's contract, so the caller's
          // object stays live at those uses: the file's findings at the lowest
          // floor are exactly the one use after the guarded destroy
          tuplesIn("destroy.c", "low") shouldBe
              Set((MemorySafetyFindingPass.RuleUseAfterFree, 51, "high"))
      }

      "stay silent on a socket released with close" in {
          // a socket is a descriptor of the same family open returns, so the
          // fd family's own release is correct at both closings, after the
          // failed connect and at the end: neither closing is a mismatch, and
          // nothing in the file leaks
          val closings = cpg.method.name("socket_connect_close").call.name("close").l
              .flatMap(_.lineNumber.map(_.toInt))
          closings should have size 2
          closings.foreach { l =>
              linesOf(
                MemorySafetyFindingPass.RuleMismatchedFree,
                "descriptor_fd.c",
                "low"
              ) should not contain l
          }
          linesOf(MemorySafetyFindingPass.RuleLeak, "descriptor_fd.c", "low") shouldBe empty
      }

      "stay silent on an open descriptor released with close" in {
          // close is the release the fd family's descriptor exists for, so
          // the closing draws no mismatch
          val closing = cpg.method.name("open_close").call.name("close").l
              .headOption.flatMap(_.lineNumber.map(_.toInt))
          closing shouldBe defined
          closing.foreach { l =>
              linesOf(
                MemorySafetyFindingPass.RuleMismatchedFree,
                "descriptor_fd.c",
                "low"
              ) should not contain l
          }
      }

      "report a stream released with close as a mismatched deallocation" in {
          // the FILE * from fopen belongs to the file family and close is
          // the fd family's release; the fixture's own prototype is what
          // lets the stream reach the descriptor release unchanged
          val rule = MemorySafetyFindingPass.RuleMismatchedFree
          val f    = findingAt(rule, "descriptor_stream.c", 14, "low").hcursor
          f.get[String]("confidence").toOption shouldBe Some("high")
          f.get[String]("kind").toOption shouldBe Some("mismatched-deallocation")
          f.get[String]("cwe").toOption shouldBe Some("CWE-762")
      }

      "report a descriptor released with fclose as a mismatched deallocation" in {
          // the descriptor from open belongs to the fd family and fclose is
          // the file family's release, the mirror of the stream mismatch
          val rule = MemorySafetyFindingPass.RuleMismatchedFree
          val f    = findingAt(rule, "descriptor_fd.c", 51, "low").hcursor
          f.get[String]("confidence").toOption shouldBe Some("high")
          f.get[String]("kind").toOption shouldBe Some("mismatched-deallocation")
          f.get[String]("cwe").toOption shouldBe Some("CWE-762")
      }

      "report a second close of a descriptor as a double close" in {
          // the first close releases the descriptor and the second releases
          // it again, so the finding sits on the second closing only
          val rule = MemorySafetyFindingPass.RuleDoubleClose
          val f    = findingAt(rule, "descriptor_fd.c", 63, "low").hcursor
          f.get[String]("confidence").toOption shouldBe Some("high")
          f.get[String]("kind").toOption shouldBe Some("double-close")
          f.get[String]("cwe").toOption shouldBe Some("CWE-1341")
      }

      "render the cleanup attribute's findings with no leak for a scope-released block" in {
          // every finding below was read against the fixture's source. No leak
          // anywhere: the cleanup call releases the block at each exit of the
          // scope, so cleanup_ok never leaks and the dangling and stolen
          // returns are not leaks either. The hand free of cleanup_double_free
          // followed by the scope's release renders as the double free on the
          // cleanup call that precedes `return v;` (line 31); the explicit
          // free at line 30 is not a finding site. The dangling return draws
          // nothing inside its own function, because the caller's use of the
          // returned block is where that defect reports, and the stolen return
          // stays silent because the steal emptied the variable first.
          File.usingTemporaryDirectory("atom-cleanup-attribute") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "cleanup.c").writeText(cleanupSource)
            cleanupFindings("cdt", dir) shouldBe Set(
              (MemorySafetyFindingPass.RuleIntegerOverflow, 13, "low"),
              (MemorySafetyFindingPass.RuleUncontrolledSize, 24, "low"),
              (MemorySafetyFindingPass.RuleUnboundedCopy, 28, "medium"),
              (MemorySafetyFindingPass.RuleDoubleFree, 31, "high"),
              (MemorySafetyFindingPass.RuleUncontrolledSize, 38, "low"),
              (MemorySafetyFindingPass.RuleUnboundedCopy, 41, "medium"),
              (MemorySafetyFindingPass.RuleUncontrolledSize, 56, "low"),
              (MemorySafetyFindingPass.RuleUnboundedCopy, 59, "medium")
            )
          }
      }

      "report the caller's use of a block the callee's cleanup call released" in {
          // the callee returns its own cleanup variable, so the scope's
          // release runs before the caller ever sees the value: both uses
          // the caller makes of the freed block are the use after free,
          // the write through it and the return-position read
          File.usingTemporaryDirectory("atom-cleanup-return-use") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "cleanup_return_use.c").writeText(cleanupReturnUseSource)
            cleanupFindings("cdt", dir) shouldBe Set(
              (MemorySafetyFindingPass.RuleUseAfterFree, 23, "high"),
              (MemorySafetyFindingPass.RuleUseAfterFree, 24, "high"),
              (MemorySafetyFindingPass.RuleUncontrolledSize, 10, "low"),
              (MemorySafetyFindingPass.RuleUnboundedCopy, 13, "medium")
            )
          }
      }

      "stay silent when a steal empties the cleanup variable before the scope ends" in {
          // ownership leaves through the steal helper and the stolen pointer
          // frees the block, so the scope's cleanup call releases the NULL
          // the steal left: no double free, no use after free, no leak
          File.usingTemporaryDirectory("atom-cleanup-steal") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "cleanup_steal_scope.c").writeText(cleanupStealSource)
            cleanupFindings("cdt", dir) shouldBe Set(
              (MemorySafetyFindingPass.RuleUncontrolledSize, 19, "low"),
              (MemorySafetyFindingPass.RuleUnboundedCopy, 24, "medium")
            )
          }
      }

      "stay silent when the hand free resets the cleanup variable before the scope ends" in {
          // the free-and-reset idiom is what makes a later release of the
          // same variable safe: the scope's cleanup call frees the NULL the
          // reset left, so the file carries no allocation-state finding
          File.usingTemporaryDirectory("atom-cleanup-reset") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "cleanup_reset.c").writeText(cleanupResetSource)
            cleanupFindings("cdt", dir) shouldBe Set(
              (MemorySafetyFindingPass.RuleUncontrolledSize, 10, "low"),
              (MemorySafetyFindingPass.RuleUnboundedCopy, 14, "medium")
            )
          }
      }

      "report a cleanup variable stored into a global at the store, at medium" in {
          // the global outlives the scope whose cleanup call releases the
          // block it keeps, so the store hands out freed memory: the finding
          // sits on the store itself and is visible at the medium floor
          File.usingTemporaryDirectory("atom-cleanup-global") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "cleanup_global.c").writeText(cleanupGlobalSource)
            cleanupFindings("cdt", dir) shouldBe Set(
              (MemorySafetyFindingPass.RuleUseAfterFree, 16, "medium"),
              (MemorySafetyFindingPass.RuleUncontrolledSize, 12, "low"),
              (MemorySafetyFindingPass.RuleUnboundedCopy, 15, "medium")
            )
            cleanupFindings("cdt", dir, "medium") shouldBe Set(
              (MemorySafetyFindingPass.RuleUseAfterFree, 16, "medium"),
              (MemorySafetyFindingPass.RuleUnboundedCopy, 15, "medium")
            )
          }
      }

      "give both C front ends the same findings on the cleanup attribute" in {
          // the cleanup calls both front ends emit must drive the overlay to
          // the same findings: same rules, same lines, same confidences, on
          // fixtures that carry a correct cleanup, a double free, a dangling
          // return, a stolen one, a caller's use of a dangling return, a
          // free followed by a reset, and a store into eternal storage
          assume(EdgaRunner.locate().isDefined, "edga is not installed")
          cleanupFixtures.foreach { (name, source) =>
              File.usingTemporaryDirectory("atom-cleanup-attribute") { dir =>
                val src = (dir / "src").createDirectory()
                (src / name).writeText(source)
                val cdt = cleanupFindings("cdt", dir)
                val edg = cleanupFindings("edg", dir)
                withClue(s"for $name: ") {
                    cdt should not be empty
                    edg shouldBe cdt
                }
              }
          }
      }
      "report a bump pool's free of pool memory and the use after its destroy" in {
          // pool_alloc hands out p->base + p->used, so its result belongs to
          // the pool: pool_ok leaks nothing and owes no heap free, the hand
          // free of pool memory is the non-heap release, and the read after
          // pool_destroy is the use after free. Both findings are high, so
          // every confidence floor sees the same file.
          File.usingTemporaryDirectory("atom-region-pool") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "probe_pool.c").writeText(poolProbeSource)
            val atLow = regionFindings("cdt", dir)
            atLow shouldBe Set(
              (MemorySafetyFindingPass.RuleNonHeapFree, 39, "high"),
              (MemorySafetyFindingPass.RuleUseAfterFree, 50, "high")
            )
            regionFindings("cdt", dir, "medium") shouldBe atLow
            regionFindings("cdt", dir, "high") shouldBe atLow
          }
      }

      "report the declared region roles through --memory-api-config" in {
          // the config's profile selects for the units that include pgmem.h:
          // palloc draws from the CurrentMemoryContext global, ctx_reset
          // empties it, ctx_destroy tears it down. The matched palloc/pfree
          // pair draws nothing but the unguarded size's own low finding, the
          // pfree of malloc memory is the family mismatch, and the context
          // memory's uses after the reset and the destroy report at medium
          // and high.
          File.usingTemporaryDirectory("atom-region-declared") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "pgmem.h").writeText(declaredRegionHeader)
            (src / "pg_user.c").writeText(declaredRegionSource)
            val config = (dir / "region-apis.json")
            config.writeText(regionApiConfig)
            val opts = Seq("--memory-api-config", config.pathAsString)
            regionFindings("cdt", dir, extra = opts*) shouldBe Set(
              (MemorySafetyFindingPass.RuleUncontrolledSize, 8, "low"),
              (MemorySafetyFindingPass.RuleUncontrolledSize, 18, "low"),
              (MemorySafetyFindingPass.RuleMismatchedFree, 20, "high"),
              (MemorySafetyFindingPass.RuleUncontrolledSize, 27, "low"),
              (MemorySafetyFindingPass.RuleUseAfterFree, 30, "medium"),
              (MemorySafetyFindingPass.RuleUncontrolledSize, 36, "low"),
              (MemorySafetyFindingPass.RuleUseAfterFree, 39, "high")
            )
            regionFindings("cdt", dir, "medium", extra = opts*) shouldBe Set(
              (MemorySafetyFindingPass.RuleMismatchedFree, 20, "high"),
              (MemorySafetyFindingPass.RuleUseAfterFree, 30, "medium"),
              (MemorySafetyFindingPass.RuleUseAfterFree, 39, "high")
            )
          }
      }

      "stay silent on a region-full fallback whose caller frees only the fallback arm" in {
          // the pool is full, so the allocator's fallback hands back a real
          // allocation and the caller frees that arm by hand before the
          // destroy takes the pool's own blocks away: neither arm leaks and
          // the guarded free of a both-arms block is not a release the heap's
          // rules own. The fallback arm's unguarded size is its own low
          // finding, and nothing survives the medium floor.
          File.usingTemporaryDirectory("atom-region-fallback") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "pool_fallback.c").writeText(poolFallbackSource)
            regionFindings("cdt", dir) shouldBe
                Set((MemorySafetyFindingPass.RuleUncontrolledSize, 22, "low"))
            regionFindings("cdt", dir, "medium") shouldBe empty
            regionFindings("cdt", dir, "high") shouldBe empty
          }
      }

      "report a use after the release that reached zero, and a second release below it" in {
          // obj_unref concludes release:1 from its body (the field decrement and the
          // zero-gated free in one), obj_new's calloc is the owned reference and
          // obj_ref's field increment is the acquire. The read after the release that
          // spent the object's own reference is the use after free at medium, and the
          // release of the spent reference is the double free at medium. The balanced
          // pair on a borrowed parameter nets the acquired reference out, the count
          // store this frame wrote keeps the object past the release, and the spent
          // release with no later use draws nothing, at every floor.
          File.usingTemporaryDirectory("atom-refcount-release") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "refcount.c").writeText(refcountSource)
            val atLow = regionFindings("cdt", dir)
            atLow shouldBe Set(
              (MemorySafetyFindingPass.RuleUseAfterFree, 31, "medium"),
              (MemorySafetyFindingPass.RuleDoubleFree, 40, "medium")
            )
            regionFindings("cdt", dir, "medium") shouldBe atLow
          }
      }

      "report the use after a borrowed release at low" in {
          // the object arrives as a parameter, so the release drops a reference whose
          // ownership the method cannot account for: the object is only maybe gone,
          // and the read after it is the weakest claim, below the medium floor. The
          // same release with no later use stays silent.
          File.usingTemporaryDirectory("atom-refcount-borrowed") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "borrowed.c").writeText(borrowedSource)
            regionFindings("cdt", dir) shouldBe
                Set((MemorySafetyFindingPass.RuleUseAfterFree, 30, "low"))
            regionFindings("cdt", dir, "medium") shouldBe empty
          }
      }

      "report the declared release and acquire roles through --memory-api-config" in {
          // the config's profile selects for the units that include refops.h:
          // ref_put drops the reference of its argument and ref_get takes one, the
          // roles' values being the parameter indexes. The declared release reads the
          // same states the inferred one does: the read after the release that spent
          // the object's own reference is the use after free at medium, the release
          // of the spent reference is the double free at medium, and the count store,
          // the balanced pair and the spent release stay silent.
          File.usingTemporaryDirectory("atom-refcount-declared") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "refops.h").writeText(declaredRefHeader)
            (src / "decl_user.c").writeText(declaredRefSource)
            val config = (dir / "ref-apis.json")
            config.writeText(declaredRefConfig)
            val opts  = Seq("--memory-api-config", config.pathAsString)
            val atLow = regionFindings("cdt", dir, extra = opts*)
            atLow shouldBe Set(
              (MemorySafetyFindingPass.RuleUseAfterFree, 11, "medium"),
              (MemorySafetyFindingPass.RuleDoubleFree, 20, "medium")
            )
            regionFindings("cdt", dir, "medium", extra = opts*) shouldBe atLow
          }
      }

      "stay silent on a release in a loop body and on the goto error path" in {
          // every element of the loop drops its reference in its own iteration, so
          // one element's release must not spend the next iteration's, and the error
          // path releases before the jump with nothing after it, so its state must
          // not reach the other path: neither control draws a finding at any floor.
          File.usingTemporaryDirectory("atom-refcount-controls") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "controls.c").writeText(refControlsSource)
            regionFindings("cdt", dir) shouldBe empty
            regionFindings("cdt", dir, "medium") shouldBe empty
          }
      }
  }
end MemorySafetyCommandTests
