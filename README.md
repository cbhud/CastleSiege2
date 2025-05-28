
# Castle Siege 2 — The Improved Version of Castle Siege!

![Castle Siege 2](https://imgur.com/undefined)

Castle Siege 2 is the enhanced and community-driven successor to the original Castle Siege! Built with the needs of players and server owners in mind, it introduces a **simple multi-arena system**, **arena regeneration**, and **full customization** of features.

> **Note:** This is a beta release. Some features may be missing or incomplete but will be continuously improved.

---

## 🔥 Key Features

- ✅ Multi-arena support
- ♻️ Arena regeneration via WorldEdit
- 🎮 Customizable kits, messages, and scoreboards (now with gradient support!)
- 🧩 Full YAML configuration: `arenas.yml`, `config.yml`, `messages.yml`, `scoreboards.yml`

---

## 🚀 Getting Started

Watch the full setup tutorial on YouTube:  
📺 [Video Tutorial](https://youtu.be/vcgLJUzd73k)

### 1. Set the Main Lobby Spawn

```bash
/setlobby
```
> Requires `OP` status or `cs.admin` permission.

---

### 2. Import and Teleport to Arena World

```bash
/mv import arena normal
/mv tp arena
```
> Requires [Multiverse-Core](https://www.spigotmc.org/resources/multiverse-core.390/) installed.

---

### 3. Create an Arena

```bash
/arena create <arenaName>
```

**Example:**
```bash
/arena create arena1
```

---

### 4. Set Arena Locations

Set the pre-game lobby for the arena:
```bash
/arena setlobby
```

Set spawn locations for each role:
```bash
/arena setking
/arena setdefenders
/arena setattackers
```

---

### 5. Select Arena Regeneration Area

Use WorldEdit to define the regeneration area.

**Steps:**
1. Get a wand:
   ```bash
   //wand
   ```
2. Select two red wool markers placed in the map (as shown in the video).
3. Use WorldEdit to select the region:
   - Left-click = First position
   - Right-click = Second position
4. Copy the selection:
   ```bash
   //copy
   ```

---

### 6. Finalize Arena Setup

```bash
/arena finish
```

---

## ⚙️ Configuration Files

Customize gameplay using the following files:

- `arenas.yml` – Arena data and locations
- `config.yml` – Core plugin settings
- `scoreboards.yml` – Scoreboard layouts
- `messages.yml` – All in-game messages (supports gradients!)

---

## 🐛 Need Help?

If you encounter any bugs or need help, contact me:

- Discord: **cbhud**
- Support Server: [Join Discord](https://discord.gg/EC3gcUsGcV)

---

Made with ❤️ for the Minecraft community.
