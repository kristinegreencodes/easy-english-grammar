# Easy English Grammar 🌸

An Android app that teaches every major English grammar rule through anime-style stories.

Hana, a curious student, climbs through ten story arcs with Sensei Fukurō the owl, Kaito the
reckless swordsman, Yumi the trickster kitsune, Chrono the time wizard and Kage of the
Punctuation Ninja Clan. Every rule is an episode with five tabs:

- **📖 Story**: a manga-style scene, revealed panel by panel
- **🧩 Parts**: the building blocks of the rule
- **📜 History**: why English works this way (the legend behind the rule)
- **✏️ Examples**: right ✅ and wrong ❌ side by side, plus a memory spell
- **🎯 Quiz**: ace it to master the rule ⭐

## Story arcs

1. Parts of Speech: The Eight Clans of Wordland
2. Nouns & Pronouns: The Village of Many Names
3. Verb Tenses: Chrono's Tower of Time
4. Verb Power-Ups: The Dojo of Special Moves
5. Sentence Structure: Building the Sentence Castle
6. Describing Words: The Painter's Garden
7. Articles & Prepositions: The Tiny Spirits Who Run Everything
8. Punctuation: The Punctuation Ninja Clan
9. Conditionals & Advanced: The Crossroads of If
10. Commonly Confused Words: Yumi's Trickster Forest

## Install on your phone

Every push to `main` builds the app on GitHub Actions and publishes it as a release.
On your Android phone, open the repo's **Releases** page, download `EasyEnglishGrammar.apk`,
and open it. Android will ask you to allow installing apps from your browser the first time.

## Adding or editing rules

All lesson content lives in `app/src/main/assets/grammar/`, one JSON file per story arc.
Add a rule by copying an existing entry. Story speakers are `sensei`, `hana`, `kaito`, `yumi`,
`chrono`, `kage` and `narrator` (see `Cast` in `data/Models.kt`).

## Build locally

Open the project in Android Studio, or run `./gradlew assembleDebug` with the Android SDK installed.
