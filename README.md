[![Build](https://github.com/pmonks/discljord-utils/workflows/build/badge.svg?branch=main)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3Abuild) [![Lint](https://github.com/pmonks/discljord-utils/workflows/lint/badge.svg?branch=main)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3Alint) [![Dependencies](https://github.com/pmonks/discljord-utils/workflows/dependencies/badge.svg?branch=main)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3Adependencies) [![Open Issues](https://img.shields.io/github/issues/pmonks/discljord-utils.svg)](https://github.com/pmonks/discljord-utils/issues) [![License](https://img.shields.io/github/license/pmonks/discljord-utils.svg)](https://github.com/pmonks/discljord-utils/blob/main/LICENSE)

# discljord-utils

A little library that extends the [`discljord`](https://github.com/IGJoshua/discljord) Clojure client library for [Discord](https://discord.com/), with:

1. Handy utility methods that support common bot operations.
2. A micro-framework that handles startup, configuration, and logging.

These can be used independently; use of the utility methods does not require use of the framework, and vice versa.

## Using the library

Express a git dependency in your `deps.edn`:

```edn
{:deps {pmonks/discljord-utils {:git/url "https://github.com/pmonks/discljord-utils.git"
                                :git/sha "92e48d38f74f77b0e55c6d15bba42615a9fe707b"}}}   ; Note: best to use the latest SHA until such time as this is deployed to Clojars
```

### Using the utility namespaces

Require either or both of the utility namespaces in your namespace(s):

```clojure
(ns your.namespace
  (:require [discljord-utils.util         :as u]     ; Handy utility methods that are not Discord / discljord specific
            [discljord-utils.message-util :as mu]))  ; Handy utility methods related to Discord / discljord messages
```

Require either or both of the included namespaces at the REPL:

```clojure
(require '[discljord-utils.util         :as u])
(require '[discljord-utils.message-util :as mu])
```

#### API Documentation

Coming soon.  For now, best to [browse the source](https://github.com/pmonks/discljord-utils/tree/main/src) and/or make liberal use of the `doc` fn at the REPL.

### Using the micro-framework

For now your best bet is to look at the [`for-science` bot](https://github.com/pmonks/for-science) as a fully-functional example of how to use the framework.  It's a little cluttered with Heroku specific paraphernalia (none of which is required by the micro-framework), but at a minimum you will need:

1. A [namespace containing your bot's responsive commands](https://github.com/pmonks/for-science/blob/main/src/for_science/commands.clj) (note: the intent is to replace this with application/slash commands once a `discljord` release is available that supports them)
2. A config file for your bot (the [`for-science` config file](https://github.com/pmonks/for-science/blob/main/resources/config.edn) demonstrates the minimal required elements, and you can add whatever bot-specific configuration elements you need to this file as well)
3. [Logback configuration](https://github.com/pmonks/for-science/blob/main/resources/logback.xml)
4. To use the `bot.main` namespace as the [entry point for your bot](https://github.com/pmonks/for-science/blob/1b4d73030da1ed8abe4310ad75bf44eb8087fcdd/deps.edn#L28-L30), rather than any of your own namespaces

Optionally, you can also provide:

1. A [build info file](https://github.com/pmonks/for-science/blob/main/resources/build-info.edn), which (if present) will be used by the framework to report the precise version of the code it is running with. You can see an example of generating this file automatically [here](https://github.com/pmonks/for-science/blob/1b4d73030da1ed8abe4310ad75bf44eb8087fcdd/bin/release.sh#L50-L58).
2. A [privacy policy](https://github.com/pmonks/for-science/blob/main/PRIVACY.md), which the bot will link to when a user issues the `!privacy` command

## Contributor Information

[Contributing Guidelines](https://github.com/pmonks/discljord-utils/blob/main/.github/CONTRIBUTING.md)

[Bug Tracker](https://github.com/pmonks/discljord-utils/issues)

[Code of Conduct](https://github.com/pmonks/discljord-utils/blob/main/.github/CODE_OF_CONDUCT.md)

## License

Copyright © 2020 Peter Monks

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

SPDX-License-Identifier: [Apache-2.0](https://spdx.org/licenses/Apache-2.0)
