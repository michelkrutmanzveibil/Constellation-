# Home

Home is not a launcher. Home is not an AI assistant. Home is a universal
input bar that lets you complete common tasks without leaving the Home
Screen.

> Do more without leaving the Home Screen.

The widget holds exactly two things: one search bar, and five app icons
(Google Maps, Spotify, YouTube, Uber, ChatGPT) underneath, drawn from their
real, currently-installed adaptive icons. Type a query, tap a provider, and
the widget expands downward into that provider's single smallest useful
action — a top-5 list, a single card, or a short answer. Tap a second
provider and the first collapses; tap outside, finish the action, or press
back and the whole thing collapses again.

## Architecture

```
app/src/main/kotlin/com/home/widget/
  core/
    provider/     Provider interface, ProviderResult/ResultItem/ResultCard
                   models, ActionOutcome, ProviderRegistry (manual DI)
    network/       Shared OkHttp/Retrofit/kotlinx.serialization setup
    icons/         Real adaptive-icon lookup via PackageManager
    location/      Best-effort last-known location (no Play Services)
    ui/            Material3 theme + the widget's shared Compose views
  providers/
    maps/ spotify/ youtube/ uber/ chatgpt/   One package per provider
  widget/          The real AppWidgetProvider + the trampoline activity
  viewmodel/       WidgetViewModel (MVVM) — the widget's only piece of state
```

Every provider implements one interface:

```kotlin
interface Provider {
    val id: String
    val displayName: String
    val packageName: String
    fun icon(context: Context): Drawable?
    fun isInstalled(context: Context): Boolean
    suspend fun search(context: Context, query: String): ProviderResult
    suspend fun performAction(context: Context, actionId: String, query: String): ActionOutcome
    fun openFullApp(context: Context, query: String)
}
```

The widget and its view model only ever talk to `Provider` and
`ProviderResult` — there is no Maps/Spotify/YouTube/Uber/ChatGPT-specific
code anywhere outside `providers/`. Adding a sixth provider means writing
one new package that implements `Provider` and adding one line to
`ProviderRegistry`; nothing else in the app changes.

`ProviderResult` is deliberately generic (`TopFive`, `SingleCard`, `Answer`,
`Empty`, `Unavailable`) so the shared result views in `core/ui/components`
never need to know which provider produced them.

## How the expand animation really works

This is the one place where the spec's language ("the widget expands
downward") runs into a real Android platform limit, and it's worth being
honest about rather than quietly papering over.

A real Home Screen widget is rendered with `RemoteViews` (or Glance, which
is a `RemoteViews` wrapper). `RemoteViews` cannot run Compose, cannot hold a
focused text field, cannot call the network, and cannot animate a layout
change. It can only lay out a fixed set of views and attach `PendingIntent`s
to them. So the collapsed widget you see on the Home Screen — the search
bar and five icons in `widget_collapsed.xml`, built by
`HomeAppWidgetProvider` — is genuinely the most a widget can be.

Every tap on it (the search bar, or any icon) launches `WidgetHostActivity`:
a translucent, chrome-less, no-title activity that is styled to be visually
indistinguishable from the idle widget. That activity is where the actual
expand/collapse experience lives — it hosts the real Compose UI, runs the
provider's network call, and uses `Modifier.animateContentSize()` to grow
the result panel downward as the response comes in, capped at ~38% of the
screen height so it never scrolls. Tapping outside the card, finishing an
action, or pressing back collapses the panel and finishes the activity,
which reads as "the widget collapsing" because the activity never looked
like anything other than the widget.

This is the same technique used by other Home Screen search widgets that
offer a richer-than-RemoteViews experience: the widget is a trampoline, and
the "app" the user perceives is actually a tightly-choreographed activity.
It is not a popup, a dialog, a bottom sheet, or a visibly separate screen
from the user's point of view, but it is worth naming precisely what's
really happening rather than letting "the widget expands" imply RemoteViews
is doing something it structurally cannot do.

## Providers: what's real, and what gracefully degrades

Every provider calls a real, documented API or Android intent. None of them
fabricate a result they don't actually have — when a key is missing, a call
fails, or an API genuinely can't answer inline, the provider returns
`ProviderResult.Unavailable` and the UI falls back to a single
"Open `<App>` →" affordance with the query preserved, exactly as the spec
requires.

- **Maps** — Google Places API (New) `places:searchText`, biased to the
  device's last-known location (framework `LocationManager`, no Play
  Services dependency). Top 5 places, each with real distance (computed
  on-device from real coordinates) and a "Navigate" action that launches
  turn-by-turn navigation via `google.navigation:q=lat,lng`.
- **Spotify** — Spotify Web API search, authenticated via the Client
  Credentials flow (app-only auth; no end-user Spotify login, since search
  is public catalog data). Top 5 tracks. "Play" opens the `spotify:track:…`
  URI, which hands off to the Spotify app and starts playback there. *True
  in-widget playback that never leaves Home would need the Spotify App
  Remote SDK (separate app auth, native library, Premium-only) — that's
  real additional scope we did not take on, not something we fake here.*
- **YouTube** — YouTube Data API v3 `search.list` + `videos.list` for
  duration. Top 5 videos with thumbnail and duration. "Play" opens
  `vnd.youtube:<id>`, which hands off to the YouTube app. If the device
  supports the platform's public Picture-in-Picture API and YouTube is
  installed, the YouTube app itself is able to continue as PiP when the
  user navigates away — that's the YouTube app's own capability, not
  something Home embeds or simulates.
- **Uber** — destination resolution via the on-device `Geocoder` (no API
  key needed), then Uber's officially documented Trip Planning universal
  deep link (`m.uber.com/ul/?action=setPickup&pickup=my_location&dropoff[…]`)
  with pickup pre-filled to the device's location. *Uber's real price and
  ETA require the OAuth Ride Request API — a full per-user login flow that
  is real additional scope. Rather than invent numbers, the card shows only
  what's real (the resolved destination) and "Request Ride" hands off to
  Uber, which shows its own live price and ETA.*
- **ChatGPT** — there is no public API for the ChatGPT app to answer
  inline, so the short answer is generated via the OpenAI Chat Completions
  API (bring-your-own key, billed to your own OpenAI account, capped to a
  couple of sentences). Without a key, or if the call fails, the provider
  skips straight to opening ChatGPT with the query preserved.

## Configuration

Every API key is optional and BYO — copy `local.properties.example` to
`local.properties` (git-ignored) and fill in whichever you have:

```
HOME_MAPS_API_KEY=
HOME_SPOTIFY_CLIENT_ID=
HOME_SPOTIFY_CLIENT_SECRET=
HOME_YOUTUBE_API_KEY=
HOME_OPENAI_API_KEY=
```

A provider with no key configured still works — it just degrades straight
to "Open `<App>` →" with the query preserved. Uber and Maps' navigation
need no key at all (Geocoder and location are on-device; only the Places
text search itself needs a key).

## Building

```
./gradlew assembleDebug
```

Requires an Android SDK (`compileSdk 35`, `minSdk 26`) on `sdk.dir` in
`local.properties`. This project was written and reviewed in a sandboxed
environment with no network access to `dl.google.com`, so the Android SDK
components could not be downloaded here and the build could not be run
end-to-end in this session — build and test it locally before relying on
it. The Gradle wrapper itself (`gradlew`, `gradle/wrapper/*`) was generated
with a local Gradle 8.14.3 install and is otherwise ready to use.

## What's deliberately not here

No onboarding screens, no settings screen, no in-app navigation beyond a
single "pin the widget" button. No scrolling, pagination, tabs, or carousels
inside the expanded panel — if a provider has more than 5 results, the UI
shows "Open `<App>` →" instead of a sixth row. No fabricated prices, ETAs,
or capabilities a provider doesn't actually have.
