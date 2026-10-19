# Opera-Style Android Mobile & Tablet Web Browser

A modern, fast, and feature-rich Android mobile and tablet web browser built with Jetpack Compose and Android System WebView. Features an Opera-style UX with a top smart address bar, dynamic page progress indicator, speed dials home view, visual multi-tab switcher, complete history & bookmarks management, and a customizable/reorderable bottom toolbar.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were clarified and confirmed with the user:

- **Search Engine**: Defaulting to Google Search (`https://www.google.com/search?q=...`) for keyword queries entered into the address bar, with automatic URL protocol handling.
- **Bottom Toolbar Customization**: User can customize and reorder toolbar actions via a dedicated toolbar settings sheet (reorder/toggle actions: Back, Forward, Home, Tabs, Bookmarks, History, Desktop Mode, Share).
- **Visual Theme**: Material 3 dynamic styling with clean tonal elevation, rounded pill-shaped address bar, and responsive tablet layout (tab strip & expanded toolbar).
- **SDK Target**: Updating `minSdk` to `28` (Android 9 Pie) supporting up to the latest Android 16 (API 36).

---

## 1. Overview & Core Concept

- **What It Does**: Provides a full-fledged web browsing experience powered by Android's System WebView. Users can browse the internet, open and switch between multiple tabs in a grid view, bookmark favorite sites, quickly launch popular destinations from Speed Dials, view and clear browsing history, and customize their bottom navigation toolbar to suit their one-handed workflow.
- **Target Audience**: Mobile and tablet users who want a fast, customizable browser with quick navigation, tab switching, and one-tap speed dial access.
- **Key Value**: Delivers the ergonomic convenience of Opera Mobile (intuitive top search/address bar, bottom navigation within thumb reach, speed dials, tab management) while natively adapting between phone and tablet screen factors.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Home Screen (New Tab / Speed Dials)**:
   - Top smart search/address bar with search engine icon and voice/QR placeholder.
   - Hero Speed Dials grid: Quick access tiles (Google, YouTube, Wikipedia, GitHub, Reddit, News, etc.) with favicons and distinct colored backdrops.
   - "Add Shortcut" button opens a dialog to add custom speed dials with title & URL.
   - Recent Bookmarks and Reading/History carousel below Speed Dials.
2. **Web Page Browsing (WebView Experience)**:
   - Entering a query or URL smoothly transitions the active tab from Home to the live WebView.
   - Top bar shrinks or transforms to show current site domain, SSL lock status, and reload/stop icon.
   - Animated progress bar under the address bar tracks page loading in real-time.
   - Page actions: Star (Bookmark), Share URL, Toggle Desktop Mode, Find in Page.
3. **Tab Switcher View**:
   - Card grid displaying all open tabs with page titles, URLs, and active tab indicator.
   - Close button on each card, "+ New Tab" button, and "Close All" action.
   - On tablets, an always-visible top tab strip allows instant tab switching.
4. **History & Bookmarks Management**:
   - Bottom sheet / full view with search filtering, timestamp grouping, and swipe-to-delete or clear-all.
   - One-tap navigation to any saved link.
5. **Toolbar Customization**:
   - Settings sheet letting users reorder bottom bar buttons (Back, Forward, Home, Tabs, Bookmarks, History, Desktop Mode, Share) and toggle their visibility.

### Visual Identity & Theme
- **Design System**: Material Design 3 with dynamic color scheme, smooth corner radii (16dp-24dp cards, 28dp pill address bar).
- **Adaptive Layout**:
  - **Phone (Compact)**: Top search bar, edge-to-edge WebView, bottom customizable navigation bar with thumb-accessible tab counter badge.
  - **Tablet (Expanded/Medium)**: Top horizontal tab bar (like Chrome/Opera desktop), combined top navigation actions, split or modal dialogs for history/bookmarks.

---

## 3. Key Product Decisions & Trade-Offs

- **Android System WebView Configuration**:
  - *Chosen Approach*: Enable modern web capabilities (DOM storage, JavaScript, wide viewport, database storage, mixed content safety, custom user-agent for desktop mode toggle).
  - *Why*: Ensures modern responsive websites render accurately with high performance.
- **Tab State Management**:
  - *Chosen Approach*: `BrowserViewModel` manages a list of `BrowserTab` objects with unique UUIDs, maintaining URL, title, loading state, progress, and canGoBack/canGoForward flags.
  - *Why*: Allows switching between tabs without losing tab identities, keeping tab switching instantaneous and lightweight.
- **Local Persistence with Room**:
  - *Chosen Approach*: Room Database for persistent `BookmarkEntity`, `HistoryEntity`, and `SpeedDialEntity`.
  - *Why*: Ensures history, custom speed dials, and bookmarks persist safely across app launches while keeping the browser completely private on-device.

---

## 4. Technical Architecture & Data Strategy

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                          MainActivity                           │
│  - WindowInsets / Edge-to-Edge                                  │
│  - Responsive Window Size Detection (Phone vs Tablet)           │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                       BrowserScreen (M3)                        │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ TopBar: Tablet Tab Strip (if Expanded) + Address / Search   │ │
│ │         SSL Lock, Domain Name, Progress Bar, Reload/Star    │ │
│ ├─────────────────────────────────────────────────────────────┤ │
│ │ Content Area:                                               │ │
│ │   - If Tab is Home: SpeedDialsView + Bookmarks Section       │ │
│ │   - If Tab is Web: AndroidView(WebView) with Custom Clients │ │
│ ├─────────────────────────────────────────────────────────────┤ │
│ │ Overlays / Sheets:                                          │ │
│ │   - TabSwitcherView (Grid of open tabs)                     │ │
│ │   - HistorySheet / BookmarksSheet                           │ │
│ │   - ToolbarCustomizationDialog                              │ │
│ ├─────────────────────────────────────────────────────────────┤ │
│ │ BottomBar: Customizable Toolbar (Phone)                     │ │
│ │   [Back] [Forward] [Home] [Tabs (Count)] [Menu/Custom]      │ │
│ └─────────────────────────────────────────────────────────────┘ │
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                        BrowserViewModel                         │
│  - StateFlow<BrowserUiState> (tabs, activeTabId, currentTab)    │
│  - StateFlow<List<ToolbarAction>> (user-ordered actions)        │
│  - Web actions: navigate, reload, goBack, goForward, addTab...  │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BrowserRepository & Room DB                  │
│  - HistoryDao (insert, query, delete, clearAll)                 │
│  - BookmarkDao (insert, query, delete, isBookmarked)            │
│  - SpeedDialDao (preset default shortcuts + custom entries)     │
└─────────────────────────────────────────────────────────────────┘
```

### Component & State Mapping

| Component | Trigger / User Action | State & Repository Mutation |
|:---|:---|:---|
| **AddressBar** | User types query or URL and presses Go | Converts to valid URL or Google query; updates active tab URL; initiates load; records in history |
| **SpeedDialItem** | Tap tile (e.g. Google, Wiki, Reddit) | Navigates active tab to target URL; records history entry |
| **TabCounterButton** | Tap bottom toolbar badge | Opens `TabSwitcherView` modal; shows grid of tabs |
| **TabCard** | Tap tab card or 'X' button | Selects tab as active or closes tab; auto-opens new tab if all closed |
| **StarButton** | Tap bookmark star in address bar | Toggles `isBookmarked`; inserts or deletes from `BookmarkDao` |
| **ToolbarSettings** | Drag/reorder actions in sheet | Persists customized action order in user preferences |
| **DesktopToggle** | Tap desktop mode button | Swaps User-Agent string on WebView between mobile and desktop |

---

## 5. Implementation Steps

1. **Build Configuration & Permissions**:
   - Update `app/build.gradle.kts`: set `minSdk = 28`.
   - Update `AndroidManifest.xml`: add `android.permission.INTERNET` and `ACCESS_NETWORK_STATE`.
   - Update `metadata.json` and `res/values/strings.xml` with app name "OperaWave Browser".
2. **Data Layer (Room Database)**:
   - Define entities: `HistoryItem`, `BookmarkItem`, `SpeedDialItem`.
   - DAOs and `BrowserDatabase` with default pre-populated speed dials (Google, YouTube, Wikipedia, Reddit, GitHub, X).
   - `BrowserRepository` to access and mutate bookmarks, history, and speed dials.
3. **ViewModel & State Management**:
   - `BrowserTab` data model: ID, title, url, favicon color, progress, canGoBack, canGoForward, isDesktopMode.
   - `BrowserViewModel`: handles tab lifecycle, web navigation, search query routing, history logging, bookmark toggling, and customizable toolbar items.
4. **WebView Integration**:
   - `BrowserWebView` using Compose `AndroidView`.
   - Custom `WebViewClient` handling page start, commit, finish, URL redirects, and error states.
   - Custom `WebChromeClient` handling progress bar updates, page titles, and desktop mode support.
5. **UI Components**:
   - `AddressBar`: Top pill search bar with SSL icon, search engine indicator, clear button, and progress indicator.
   - `SpeedDialsView`: Beautiful grid of speed dial tiles, "Add Speed Dial" dialog, and quick bookmark list.
   - `TabSwitcherView`: Visual grid of cards representing open tabs, active badge, and "+ New Tab" action.
   - `BottomToolbar`: Adaptive toolbar with customizable icon list, tab count badge, and overflow/customize sheet.
   - `HistoryDialog` & `BookmarksDialog`: Filterable lists with quick launch and delete actions.
6. **Adaptive Tablet Support**:
   - Top tab strip on medium/expanded screens.
   - Responsive multi-column layout for speed dials and modal sheets.
7. **Compilation & Verification**:
   - Run `compile_applet` and verify zero errors across all components.
