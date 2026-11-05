<!--
SPDX-FileCopyrightText: 2026 klikli-dev

SPDX-License-Identifier: MIT
-->

# Principia Magica

Yet another minecraft magic mod.

Principia Magica gives Golems their time in the spotlight once again!
Automate resource transfer, crafting, farming and more with nifty little helpers.

## Golems

Golems are helpers that work for you. Each golem has a material (wood, stone or metal), a job and upgrade slots. The material decides how many upgrades it can take.

### Crafting

- **Golem**: crafted per material. Put Redstone Dust in the middle and the material in the four corners: `M _ M / _ R _ / M _ M`. Wood golems use logs, stone golems use cobblestone and metal golems use iron ingots.
- **Farming Core**: `S W S / W A W / S W S` with Wheat Seeds (S), Wheat (W) and an Amethyst Shard (A).
- **Golem Baton**: `_ A G / _ S _ / S _ _` with an Amethyst Shard (A), a Gold Ingot (G) and a Stick (S).
- **Swiftness Upgrade**: `_ S _ / F A R` with Sugar (S), Feather (F), Amethyst Shard (A) and Redstone Dust (R). Makes the golem faster.
- **Capacity Upgrade**: `L C L / _ A _` with Leather (L), a Chest (C) and an Amethyst Shard (A). Adds 3 inventory slots.

### Setting up a farm

1. Place your golem in the middle of the field. It becomes yours.
2. Right-click the golem with a Farming Core to give it the Farming job.
3. The golem gets a 9x9 work zone centred on where you placed it (1 block down and 2 up), and a chest touching that spot is linked as a container that it both takes from and deposits to.

A farming golem harvests ripe crops, replants them from seeds it carries and restocks seeds from its linked containers.

### Golem Baton

Use the baton to change where a golem works:

- Right-click a golem to select it, right-click it again to deselect.
- With a golem selected, right-click a container to cycle its role: supply, deposit, both, removed.
- Right-click two blocks (opposite corners of the zone) to create a work zone.
- Shift-right-click a zone or container to remove it.
- Shift-right-click a golem to copy its workplace, then shift-right-click another golem to paste it.
- Shift-left-click your golem to pick it up. Its workplace is kept.
- Shift-right-click air to clear the selection and clipboard.

### Golem GUI

Right-click your golem with an empty hand to open its GUI. It shows the golem's status, mode, job options, upgrades, work zones and containers. Shift-right-clicking with an empty hand removes the golem's job and gives you back its core.

- **Modes**: Work (do the job in the work zone), Follow (follow you) and Stay (stay where it is).
- **Job options** (Farming): replant crops, till soil near water (needs a hoe), apply bone meal, and pick up stray items. Hover an option for a description.
- **Upgrades**: remove an upgrade with its button in the list.

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
Assets are licensed under the CC-BY-SA-4.0 license, view [LICENSES/CC-BY-SA-4.0](./LICENSES/CC-BY-4.0.txt).  

There are third party code and assets in this project which may be under different licenses and copyrights. We follow the [REUSE Standard for Software Licensing](https://reuse.software/), so you can look up the license terms for each file, or use the [REUSE tool](https://github.com/fsfe/reuse-tool) to generate an SPDX report.
