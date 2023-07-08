import { atom, compress, join, parse } from "./deps.ts";

const persist = async (atomStruct: atom.CpgStruct, fileName = "app.atom") => {
  const protoFile = "cpg.proto";
  Deno.writeFileSync(protoFile, atomStruct.serialize());
  await compress(protoFile, fileName, { overwrite: true, flags: [] });
  Deno.removeSync(protoFile);
  console.log(fileName, "created successfully");
};

const args = parse(Deno.args);
const filePath = args.src || args._[0] as string || Deno.cwd();
const outDir = args.outDir || Deno.cwd();

console.log("Invoking atomgen with args", filePath, outDir);

const methodFullName = new atom.CpgStruct.Node.Property({
  name: atom.NodePropertyName.FULL_NAME,
  value: new atom.PropertyValue({ string_value: "main" }),
});
const method = new atom.CpgStruct.Node({
  key: 1,
  type: atom.NodeType.METHOD,
  property: [methodFullName],
});
const atomStruct = new atom.CpgStruct({ node: [method] });
await persist(atomStruct, join(outDir, "app.atom"));
