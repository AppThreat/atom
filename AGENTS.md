# AGENTS.md

Guidance for AI coding agents and for people working on this repository.

## Crediting contributors

People outside the project report bugs, track down root causes, write reproducers and post sample code. When a change uses that work, credit them in the commit message. A thank-you in an issue comment reaches neither the git history nor the release notes.

- `Reported-by: Name <email>` names the person who filed the bug report the change fixes.
- `Helped-by: Name <email>` names anyone whose idea, sample code, reproducer, fixtures, investigation or earlier pull request the change uses.

Follow each one with `Link:` and the URL of that issue, comment or pull request:

```
Reported-by: Jane Doe <12345678+janedoe@users.noreply.github.com>
Link: https://github.com/OWNER/REPO/issues/123
Helped-by: John Roe <87654321+johnroe@users.noreply.github.com>
Link: https://github.com/OWNER/REPO/issues/123#issuecomment-456
```

- Take the name from the person's GitHub profile. Use their noreply address, `<id>+<login>@users.noreply.github.com` (`gh api users/<login> --jq .id` prints the id), unless they asked for a different address.
- Put the trailers in every commit that uses the work, and repeat them under `## Credits` in the pull request description. A merge commit keeps only the pull request title, so the description alone does not reach the history.
- Do not use these trailers for bots, AI tools or yourself.
