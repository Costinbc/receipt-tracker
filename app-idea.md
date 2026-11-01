# App idea

## The basics

- **App name:** ReceiptTracker
- **One-line pitch:** Photograph a receipt, and the app reads the total, date and merchant so you can
  track your spending, even with no signal.
- **The problem:** Paper receipts get lost, and typing every expense in by hand is tedious, so people
  stop tracking. Most expenses happen in shops, markets and abroad, often with weak or no connection.
- **Target users:** Students and young professionals tracking a monthly budget, who log a purchase
  right at the till or later from a pile of receipts, sometimes in a basement shop or abroad without
  roaming.

## The prototype scope

- **Core screen(s):** An expense list (newest first, with the month's total at the top), an expense
  detail screen (all fields plus the receipt photo), and an "add expense" form.
- **What the app manages:** an **Expense**. In the finished app an expense is always created by
  photographing the receipt; typing one in by hand stays as a fallback, for when the receipt is
  gone or unreadable. End-of-month statistics by category and merchant come in a later phase.
  For this prototype we only built and tested adding an expense by hand, so that the data model,
  navigation and state handling are settled before the camera and OCR go on top.
- **A few fields of an expense:** id, merchant, total, currency, date, category (food, transport,
  ...), note, receipt photo.

## Planned features

Beyond the prototype, these are the features we want, each with the mobile-computing problem it
raises:

- **Offline expense tracking.** Receipts and expenses are saved locally with no internet, and
  synchronised across devices when the connection returns.
- **Budget tracking and alerts.** Current spending is compared against a locally cached budget and
  alerts fire instantly, offline. Longer-term analysis recommends budget limits.
- **Smart expense search.** Instant search over cached receipts by store, date or amount, and
  natural-language questions such as "how much did I spend on coffee last semester?".
- **Subscription and recurring expense detection.** Historical expenses are analysed to spot
  subscriptions and repeat purchases, and the user is reminded before the next payment.
- **Receipt storage optimisation.** Images are compressed, recent receipts are cached and stale
  local copies are dropped, while full-resolution originals are kept in the backup.
- **Translation of foreign receipts.** On-device ML translation so a receipt in another language
  still yields a merchant and a total, which matters most when abroad and offline.

## The data

- **What records does the app create or collect?** Expenses, normally extracted from a receipt
  photo and otherwise entered by hand. Categories are a small fixed list.
- **Where does data come from?** The camera (receipt photos), text recognition on the photo (OCR,
  on the device or in the cloud), and the user (corrections and manual entries). A public
  exchange-rate API is added in a later stage for expenses in other currencies.
- **Where does data need to go?** Expenses and photos are saved locally first, then synced to a
  backend (Firebase or a simple REST server) for backup and for use on another device.

## Mobile-computing framing (required)

- **What must persist on the device:** All expenses (the local database is the source of truth),
  receipt photos that have not been uploaded yet, the queue of pending changes to sync, and the last
  known exchange rates.
- **What crosses the network:** Syncing expenses and receipt photos to the backend, exchange rates
  from a public API, and, only on the cloud path, the receipt image sent to a cloud OCR service.
- **What must still work with no signal:** Everything the user does: taking a receipt photo,
  extracting the fields with on-device OCR, adding, editing and browsing expenses, and seeing
  totals. Changes are marked "pending sync" and uploaded by a background job when the connection
  returns. Currency conversion uses the last cached rate and shows when it was fetched.
- **The placement question:** Receipt text recognition can run on the device (ML Kit text
  recognition) or in the cloud (a cloud vision or multimodal model). Both sit behind one
  `ReceiptScanner` interface, and we plan to measure them on the same set of receipts.
  - **On the device:** works offline, the receipt never leaves the phone (privacy: it shows where
    you shop), no upload cost, and low, predictable latency. It may be less accurate on crumpled or
    faded receipts, and it uses the phone's CPU and battery.
  - **In the cloud:** probably more accurate and better at understanding the layout (which number is
    the total), but it needs a connection, uploads the image (bandwidth, radio energy, latency that
    depends on the network) and may cost money per request.
  - **Current plan:** on the device by default, with the cloud as an optional fallback when online
    and on-device confidence is low. We will compare latency, accuracy and bytes sent to justify it.
