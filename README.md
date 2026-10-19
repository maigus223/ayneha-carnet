# AYNEHA Carnet

Petit carnet de notes **hors-ligne**, 100 % interface AYNEHA (zéro caractère latin dans l'interface, sauf l'écran Confidentialité).
Version 1.1.0 - développé par MAIGUS, avec l'aide d'outils d'IA (Claude, Anthropic).

## Licences

- Code : GPL v3 ou ultérieure (fichier `LICENSE`, en-têtes SPDX dans chaque fichier source).
- Police AYNEHA (`ayneha_pro.ttf`) : SIL Open Font License 1.1 (fichier `OFL.txt`). Nom et logo AYNEHA réservés.
- Logos et textes : CC BY-SA 4.0.
- Code source : github.com/maigus223/ayneha-carnet

## Stack

- **Kotlin** + **Jetpack Compose**
- **Room** (SQLite local)
- Police `ayneha_pro.ttf` (v3.0 de transition : ancienne plage U+E000-E061 et nouvelle plage U+10FF7C-U+10FFDD)
- Partage = **JPG uniquement** (les autres applications ne lisent pas les glyphes AYNEHA)

## Clavier

Le Carnet n'embarque plus de clavier à l'écran : il utilise le **clavier AYNEHA officiel** (le même que AYNEHA Keyboard 2.5.0, dont le code est inclus ici sous `com.ayneha.keyboard` pour éviter d'avoir deux claviers). Activer "AYNEHA Carnet" dans les réglages Android, puis le choisir comme clavier.

Chaque glyphe est inséré sous la forme RLO + glyphe + PDF (pur RTL). Pour que l'effacement reste toujours lettre par lettre, de droite à gauche, les champs du Carnet répartissent la saisie à travers `AynehaRtlTransformation` (`util/AynehaRtl.kt`) : après chaque modification, toute enveloppe abîmée (RLO ou PDF orphelin, moitié de glyphe) est réparée ou retirée.

## Plage Unicode

Les glyphes AYNEHA sont dans le plan 16 (U+10FF7C-U+10FFDD) : chaque glyphe occupe **deux** unités UTF-16 (paire de substitution). Le code ne doit donc jamais utiliser `length`, `[i]`, `substring`, `reversed()` ou `takeLast` sur du texte AYNEHA (utiliser les points de code). Les notes enregistrées avec l'ancienne plage sont converties automatiquement au premier lancement (`PlaneMigration`), ainsi que les sauvegardes importées.

## Règles respectées

| Règle | Implémentation |
|-------|----------------|
| Zéro latin UI | `AynehaStrings.kt` |
| Pas de Gras | `FontWeight.Normal` partout |
| RTL + ancrage droite | `TextDirection.Rtl` + `TextAlign.Right` |
| Titre obligatoire | `NoteEditorViewModel.save()` refuse titre vide |
| Partage | `NoteImageExporter` -> JPG -> `ACTION_SEND` |

## Ouvrir dans Android Studio

1. File -> Open -> dossier `ayneha_carnet`
2. Sync Gradle
3. Lancer sur émulateur / appareil (API 26+)
