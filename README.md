| | | |
|---:|:---:|:---:|
| [**release**](https://github.com/pmonks/discljord-utils/tree/release) | [![CI](https://github.com/pmonks/discljord-utils/actions/workflows/ci.yml/badge.svg?branch=release)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3ACI+branch%3Arelease) | [![Dependencies](https://github.com/pmonks/discljord-utils/actions/workflows/dependencies.yml/badge.svg?branch=release)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3Adependencies+branch%3Arelease) |
| [**dev**](https://github.com/pmonks/discljord-utils/tree/dev)  | [![CI](https://github.com/pmonks/discljord-utils/actions/workflows/ci.yml/badge.svg?branch=dev)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3ACI+branch%3dev) | [![Dependencies](https://github.com/pmonks/discljord-utils/actions/workflows/dependencies.yml/badge.svg?branch=dev)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3Adependencies+branch%3Adev) |

[![Latest Version](https://img.shields.io/clojars/v/com.github.pmonks/discljord-utils)](https://clojars.org/com.github.pmonks/discljord-utils/) [![Open Issues](https://img.shields.io/github/issues/pmonks/discljord-utils.svg)](https://github.com/pmonks/discljord-utils/issues) [![License](https://img.shields.io/github/license/pmonks/discljord-utils.svg)](https://github.com/pmonks/discljord-utils/blob/release/LICENSE)


# discljord-utils

A little library that extends the [`discljord`](https://github.com/IGJoshua/discljord) Clojure client library for [Discord](https://discord.com/), with:

1. Handy utility methods that support common bot operations.
2. A micro-framework that handles startup, configuration, and logging.

These can be used independently; use of the utility methods does not require use of the framework, and vice versa.

**NOTE:** This library pre-dates the implementation of application (aka "slash") commands by Discord, and the approach it implements for commands is not longer considered idiomatic.  Consider using application commands instead, perhaps via a library such as [`JohnnyJayJay/slash`](https://github.com/JohnnyJayJay/slash).

## Using the library

### Documentation

[API documentation is available here](https://pmonks.github.io/discljord-utils/).

### Dependency

Express the correct maven dependencies in your `deps.edn`:

```edn
{:deps {com.github.pmonks/discljord-utils {:mvn/version "LATEST_CLOJARS_VERSION"}}}
```

### Require one or more of the namespaces

In your namespace(s):

```clojure
(ns your.namespace
  (:require [discljord-utils.util         :as u]     ; Handy utility methods that are not Discord / discljord specific
            [discljord-utils.message-util :as mu]))  ; Handy utility methods related to Discord / discljord messages
```

At the REPL:

```clojure
(require '[discljord-utils.util         :as u])
(require '[discljord-utils.message-util :as mu])
```

### Using the micro-framework

For now your best bet is to look at the [`for-science` bot](https://github.com/pmonks/for-science) as a fully-functional example of how to use the framework.  It's a little cluttered with deployment specific paraphernalia (none of which is required by the micro-framework), but at a minimum you will need:

1. A [namespace containing your bot's responsive commands](https://github.com/pmonks/for-science/blob/release/src/for_science/commands.clj) (note: the intent is to replace this with application/slash commands)
2. A config file for your bot (the [`for-science` config file](https://github.com/pmonks/for-science/blob/release/resources/config.edn) demonstrates the minimal required elements, and you can add whatever bot-specific configuration elements you need to this file as well)
3. [Logback configuration](https://github.com/pmonks/for-science/blob/release/resources/logback.xml)
4. To use the `bot.main` namespace as the [entry point for your bot](https://github.com/pmonks/for-science/blob/release/deps.edn#L30), rather than any of your own namespaces

Optionally, you can also provide:

1. A [build info file](https://github.com/pmonks/for-science/blob/release/resources/build-info.edn), which (if present) will be used by the framework to report the precise version of the code it is running with. You can see an example of generating this file automatically [here](https://github.com/pmonks/for-science/blob/1b4d73030da1ed8abe4310ad75bf44eb8087fcdd/bin/release.sh#L50-L58).
2. A [privacy policy](https://github.com/pmonks/for-science/blob/release/PRIVACY.md), which the bot will link to when a user issues the `!privacy` command

## Contributor Information

[Contributing Guidelines](https://github.com/pmonks/discljord-utils/blob/release/.github/CONTRIBUTING.md)

[Bug Tracker](https://github.com/pmonks/discljord-utils/issues)

[Code of Conduct](https://github.com/pmonks/discljord-utils/blob/release/.github/CODE_OF_CONDUCT.md)

### Developer Workflow

This project uses the [git-flow branching strategy](https://nvie.com/posts/a-successful-git-branching-model/), and the permanent branches are called `release` and `dev`, and any changes to the `release` branch are considered a release and auto-deployed (JARs to Clojars, API docs to GitHub Pages, etc.).

For this reason, **all development must occur either in branch `dev`, or (preferably) in temporary branches off of `dev`.**  All PRs from forked repos must also be submitted against `dev`; the `release` branch is **only** updated from `dev` via PRs created by the core development team.  All other changes submitted to `release` will be rejected.

## License

Copyright © 2020 Peter Monks

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

SPDX-License-Identifier: [Apache-2.0](https://spdx.org/licenses/Apache-2.0)
