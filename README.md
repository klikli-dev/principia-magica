<!--
SPDX-FileCopyrightText: 2026 klikli-dev

SPDX-License-Identifier: MIT
-->

# Principia Magica

_"In the beginning was the Word, and the Word was Law."_

In Principia Magica you carve laws into clay and thus bend the world to your will.

## Maven

See https://cloudsmith.io/~klikli-dev/repos/mods/groups/ for available versions.

```gradle
repositories {

  ...

  maven {
    url "https://dl.cloudsmith.io/public/klikli-dev/mods/maven/"
    content {
        includeGroup "com.klikli_dev"
    }
  }

  ...

}
```

```gradle
dependencies {

    ...

    implementation "com.klikli_dev:principiamagica-${minecraft_version}-neoforge:${principia_magica_version}"

    ...

}
```

## Licensing

Copyright 2026 klikli-dev

Code is licensed under the MIT license, view [LICENSES/MIT](./LICENSES/MIT.txt).  
Assets are licensed under the CC-BY-4.0 license, view [LICENSES/CC-BY-4.0](./LICENSES/CC-BY-4.0.txt).  

There are third party code and assets in this project which may be under different licenses and copyrights. We follow the [REUSE Standard for Software Licensing](https://reuse.software/), so you can look up the license terms for each file, or use the [REUSE tool](https://github.com/fsfe/reuse-tool) to generate an SPDX report.
