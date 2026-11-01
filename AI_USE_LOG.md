# AI-use log

You are encouraged to use AI throughout this project. In return you keep this log. It is part of
every milestone, not just Lab 3, so keep adding to it all semester. It is quick if you do it as you
work and painful if you reconstruct it later.

The point is not to prove you used AI, or that you did not. The point is to show that you **own** what
you submitted: that you understood what the model produced, judged it, and decided what to keep. In
the defense you must be able to explain and modify any of it.

## How to log

One row per meaningful AI interaction. You do not need to log trivial autocompletions; do log
anything where the model produced a chunk of code or a design decision you accepted or rejected.

Each member keeps their own section (the project handout asks for this), and the log is append-only.

## Member 1: Costin Bosoaga (costinbc)

| Date | What you were trying to do | Tool + what you prompted (short) | What the model produced | What you accepted, rejected or changed, WHY, and how you verified it |
|---|---|---|---|---|
| 2026-10-31 | Find an app idea | Claude Code: gave it the handout and guidelines, asked for project ideas | A few app ideas, then feedback on our own ideas | It had bad or complicated ideas, we picked our receipt and expenses tracker idea because it has real life use and I would use it myself |
| 2026-10-31 | Build the prototype | Claude Code: I set up the git repo and told it to create a draft. Then to add the fake data. | Compose screens, fake data, navigation | Ran it on the emulator to check if it loads successfully and it did |

## Member 2: Andronic Dragos-Sebastian (AndronicD)

| Date | What you were trying to do | Tool + what you prompted (short) | What the model produced | What you accepted, rejected or changed, WHY, and how you verified it |
|---|---|---|---|---|
| 2026-11-01 | Review Costin's prototype before merging | Copilot CLI: gave it the handout and the repo, asked what breaks under the Part 4 stress tests | 10 findings, from `Double` for money to `remember` vs `rememberSaveable` | Posted most on PR #1. Two were wrong, and I only found out by running the app: `add()` does insert at index 0, and Navigation Compose does restore the detail route. I now verify findings on the emulator before posting. |
| 2026-11-01 | Store money correctly and parse the receipt total | Copilot CLI: asked if `Double` was fine for money, then for a regex parser for a messy Romanian receipt | `Long` minor units, and a keyword parser with a confidence score | Took both. Did the `Long` migration myself because it becomes the Room column at M2. Rewrote the parser heuristics: prefer the last keyword line, take the right-most amount. A Python mirror over 13 fixtures caught the regex matching `01.11.2026` as `1.11`. |
| 2026-11-01 | Test the code and the running app | Copilot CLI: asked for unit tests per trap, then to drive the emulator over adb and measure | 24 JVM tests, plus rotation, process-death and 515-item runs | A test failed for a real reason: this JVM is `ro_RO`, so `"%.2f"` gives `187,45`. `formatted()` now takes a `Locale`. Kept the measured numbers in `reflection.md`. Rejected its read of airplane mode as a pass: there is no `INTERNET` permission, so nothing could fail. |

## One thing AI got wrong, or could not tell us, on this project

AI cannot tell you what it did not look at. Its review of the prototype read as authoritative, but
two findings were false and three real bugs were missing: no date field, a hardcoded RON currency,
and an airplane-mode pass that only happens because the app never asks for `INTERNET`. All three
are invisible in the source and obvious in thirty seconds on an emulator. It was also most
confident where it should have hesitated, picking `Double` for money and writing a month total that
drops a currency. That code looks fine and returns wrong numbers. We now treat AI output as a draft
to run and measure, not a draft to read and approve.

## Prompts worth remembering

Optionally, keep a few prompts that worked especially well or especially badly, with a note on why.
This is useful for you, and for your team if you are working in one.

Note on stress testing from a prompt. Asking "is this fast enough?" only gets an opinion. What
worked was telling the model to install the app on the emulator, drive it over adb, and for each
Part 4 scenario name the command, run it, and show a screenshot. No claim without a command behind
it. That is what found the real bugs, and what caught two of our own review findings being wrong.
