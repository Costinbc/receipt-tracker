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

- **Core screen(s):** An expense list (newest first, with the month's total at the top) and an
  expense detail screen (all fields plus the receipt photo). Optionally, an "add expense" form.
- **The main "thing" the app manages:** an **Expense**, optionally backed by a receipt photo.
- **A few fields of that thing:** id, merchant, total, currency, date, category (food, transport,
  ...), note, receipt photo.

## The data

- **What records does the app create or collect?** Expenses, entered by hand or extracted from a
  receipt photo. Categories are a small fixed list.
- **Where does data come from?** The user (manual input and corrections), the camera (receipt
  photos), and text recognition on the photo (OCR, on the device or in the cloud). Optionally a
  public exchange-rate API for expenses in other currencies.
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
