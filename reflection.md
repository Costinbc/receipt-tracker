# Prototype reflection: where AI stopped

## What AI did well

- **Produced a whole working app from one prompt.** Compose screens, a navigation graph with a
  typed route, fifteen plausible Romanian expenses, Material 3 theming — list → detail → add,
  running on the emulator. Writing that by hand would have taken a day; it took minutes.
- **Drafting regexes and test fixtures.** The receipt parser started as an AI draft. It was a
  genuinely useful starting point, and it was also wrong in ways we only found by testing it
  (below).

## What broke when you stressed it

We ran the five checks on the emulator instead of reasoning about them.

- **Rotate the device:** Failed. Typing into the Add screen and rotating cleared every field.
  `remember` survives recomposition, not configuration change.
- **Kill and reopen the app:** Half failed. Navigation restored the screen, but the added expense
  was gone. The store is in memory, so nothing is written down.
- **Airplane mode:** Passed for free. The app has no `INTERNET` permission and no network code,
  so nothing could fail.
- **500 items in a list:** Degraded. `LazyColumn` was fine, but the month total rescans every
  expense on each recomposition.
- **Two people editing the same data:** No handling at all. There is no user, no server and no
  idea of a conflict.

## What is missing for production

| Gap in the prototype | Lab that fixes it |
|---|---|
| No separation of UI and logic; state resets | Lab 4 (architecture) |
| No data from a real server | Lab 5 (networking) |
| Nothing is saved on the device | Lab 6 (persistence) |
| Does not work offline; no background sync | Lab 7 (offline-first, WorkManager) |
| Dependencies wired by hand | Lab 8 (dependency injection) |
| No tests; nothing stops a broken change | Lab 9 (testing, CI) |
| No mobile-computing concept beyond a UI | Lab 10 (integrated concept) |

Gaps specific to this app:

| Gap | Where it gets fixed |
|---|---|
| **No camera and no OCR.** The entire premise — point the phone at a receipt — does not exist. Totals are typed by hand. | Lab 10, with the parser in this repo as the thing OCR feeds |
| **No date picker.** Every expense is stamped `LocalDate.now()`, so a receipt from yesterday cannot be logged. For a receipt scanner that is close to fatal. | Lab 4 (form state) |
| **Currency hardcoded to RON** on the Add screen, even though the seed data contains EUR rows the user could never create. | Lab 4 |
| **No on-device vs cloud decision.** `ParsedTotal` now carries a confidence score, but nothing consumes it yet. The privacy and payload argument in our proposal depends on that branch existing. | Lab 10 |
| **No instrumented test.** The handout requires at least one; we have JVM unit tests and zero UI tests. Lab 2's `CatalogNavigationTest` is the template. | Lab 9 |
| **Money was `Double`.** Fixed during M1 after review — now `Long` minor units — but it is worth recording that the generated prototype reached for `Double` for currency without hesitating. | fixed |

## One sentence

> AI gave us an app that is correct for the happy path it imagined and silent about every
> condition it did not — rotation, process death, scale, a second user, a receipt from yesterday —
> and it cannot close that gap for us because closing it means *choosing* what the app should do
> when things go wrong, and those choices are the design of the product, not a detail of its
> implementation.
