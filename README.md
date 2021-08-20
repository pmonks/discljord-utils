(https://github.com/pmonks/discljord-utils/tree/main) [![Build](https://github.com/pmonks/discljord-utils/workflows/build/badge.svg?branch=main)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3Abuild) [![Lint](https://github.com/pmonks/discljord-utils/workflows/lint/badge.svg?branch=main)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3Alint) [![Dependencies](https://github.com/pmonks/discljord-utils/workflows/dependencies/badge.svg?branch=main)](https://github.com/pmonks/discljord-utils/actions?query=workflow%3Adependencies)

[![Open Issues](https://img.shields.io/github/issues/pmonks/discljord-utils.svg)](https://github.com/pmonks/discljord-utils/issues)
[![License](https://img.shields.io/github/license/pmonks/discljord-utils.svg)](https://github.com/pmonks/discljord-utils/blob/main/LICENSE)

# discljord-utils

Handy utility methods for [Discord](https://discord.com/) bots implemented in Clojure, that use the [`discljord`](https://github.com/IGJoshua/discljord) client library.

## Using the library

Express a git dependency in your `deps.edn`:

```edn
{:deps {pmonks/discljord-utils {:git/url "https://github.com/pmonks/discljord-utils.git"
                                :git/sha "latest_sha_in_repo"}}}
```

Require either or both of the included namespaces in your namespace(s):

```clojure
(ns your.namespace
  (:require [discljord-utils.util         :as u]
            [discljord-utils.message-util :as mu]))
```

Require either or both of the included namespaces at the REPL:

```clojure
(require '[discljord-utils.util         :as u])
(require '[discljord-utils.message-util :as mu])
```

## API Documentation

Coming soon.  For now, best to [browse the source](https://github.com/pmonks/discljord-utils) and/or make liberal use of the `doc` fn at the REPL.

## Contributor Information

[Contributing Guidelines](https://github.com/pmonks/discljord-utils/blob/main/.github/CONTRIBUTING.md)

[Bug Tracker](https://github.com/pmonks/discljord-utils/issues)

[Code of Conduct](https://github.com/pmonks/discljord-utils/blob/main/.github/CODE_OF_CONDUCT.md)

## License

Copyright © 2020 Peter Monks

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

SPDX-License-Identifier: [Apache-2.0](https://spdx.org/licenses/Apache-2.0)
