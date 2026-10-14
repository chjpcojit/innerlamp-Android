# Versioning and changelog

## Header (top of requirements.md, design.md, tasks.md)

```markdown
# <Requirements|Design|Tasks> — <Feature>

Version: <major>.<minor>
Last updated: <YYYY-MM-DD>
Status: <one line>

> See `changelog.md` in this folder for the version history of this spec.
```

`requirements.md` also carries the `Feature folder:` line and the `Product spec:` / `Contract:` /
`Implements:` lines (see `upstream_product_spec.md`). The three files share one version number: bump
all three even when only one changed.

## Bump rule

- **Minor (+0.1)**: clarification, resolved question without restructuring, small added criterion,
  task correction, adopting a product spec MINOR or a newer contract with no behavior change.
- **Major (+1.0)**: scope change, new Phase, a step added/removed in the Step-by-step Flow, adopting
  a product spec MAJOR version.

## Entry format (newest first)

```markdown
## v<version> (<date>)

- `requirements.md`: <what and why — or "no change needed, verified">.
- `design.md`: <…>.
- `tasks.md`: <…>.
- Product spec / contract: <version adopted, IDs affected — or "unchanged">.
- Hand-offs: <to ba-writer / backend — or "none">.
- Reason: <the cause: product change, answered question, code read, …>.
```

Say explicitly when a file did not need to change. `Docs/Specs/Networking/changelog.md` is the
worked example for multi-version history.

## Initial version

Always `v1.0 (initial)`; the entry says prospective or retroactive and names the product spec
version.
