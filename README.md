# CriminalIntent Project Brief

## 1. Overview & Purpose

CriminalIntent is a lighthearted but functional Android app for documenting “office crimes” — small workplace misdemeanors like leaving dirty dishes in the sink, hoarding staplers, or walking away from a noisy printer job.  

The app’s tone is playful, but the functionality is serious enough to be genuinely useful:

- Capture incidents as structured “crime” records.
- Organize them in a scrollable list for quick reference.
- Document details with titles, dates, photos, and suspects.
- Share humorous reports through email, social media, or other compatible apps.

Although whimsical in theme, CriminalIntent is designed as a real-world, maintainable Android application that evolves over ~13 development chapters, incrementally adding features and improving architecture.

---

## 2. Core Features

- **Create crime records**
  - Add a new crime from the main list screen.
  - Each crime has a unique identifier, title, date, solved status, and optional media/metadata.

- **Edit crime details**
  - Enter and edit a crime title.
  - Mark whether the crime is “solved”.
  - Select and display the crime date (and possibly time) using appropriate date-picker UI.

- **Attach photo evidence**
  - Capture a photo using the device camera or select from available sources (as supported by the platform).
  - Display the attached photo in the detail screen as visual “evidence”.

- **Choose a suspect from contacts**
  - Integrate with the device’s Contacts provider.
  - Allow selecting a contact as the “suspect” for the crime.
  - Display the suspect’s name in the detail view once selected.

- **Generate and share a crime report**
  - Build a human-readable summary of the crime (title, date, solved status, suspect, etc.).
  - Share via an implicit intent to compatible apps (email, Twitter, Facebook, messaging apps, etc.).
  - Leverage Android’s sharing mechanisms so users can pick any installed app that can handle text content.

- **Main list interactions**
  - Show all crimes in a **scrollable list**.
  - Tap an item to open its detail screen.
  - Add new crimes using a dedicated button (e.g., FAB or menu action).
  - Reflect edits made in the detail screen back in the list view.

- **Smooth navigation**
  - Use an activity/fragment structure that makes it easy to move between list and detail.
  - Support both phone (single-pane) and, if extended later, tablet (master-detail/two-pane) layouts.

---

## 3. User Flows

- **Create a new crime**
  - From the list screen, the user taps “Add”.
  - A new crime record is created and the detail screen opens.
  - The user edits the title, sets the date, optionally attaches a photo, selects a suspect, and marks solved/not solved.
  - Changes are automatically saved and reflected in the list.

- **Edit an existing crime**
  - User scrolls through the crime list.
  - Taps a crime to open its detail view.
  - Updates title, date, suspect, solved status, and/or photo.
  - Navigates back; the list updates to show the latest information.

- **Attach and view a photo**
  - In the detail view, user taps a camera/photo button.
  - System camera or chooser is launched.
  - On success, the photo is associated with this crime and shown in the detail UI.

- **Select a suspect**
  - From the detail screen, user taps “Choose suspect”.
  - Contacts picker opens; user selects a contact.
  - The suspect field in the crime detail updates with the chosen contact’s display name.

- **Share a report**
  - User taps “Share report” in the detail screen.
  - A formatted text report is created.
  - Android’s share sheet appears; user picks an app (email, Twitter, Facebook, etc.).
  - The chosen app opens with the report pre-filled as message body/content.

---

## 4. Architecture: List–Detail Pattern

CriminalIntent follows a **list–detail architecture**, a common pattern where:

- The **List Screen** (Master)
  - Displays a collection of items (crimes) in a scrollable list.
  - Handles actions like adding a new crime or selecting one to view/edit.
  - Observes the underlying data so the UI refreshes when crimes are added or updated.

- The **Detail Screen** (Detail)
  - Shows a single crime’s full details.
  - Provides controls for editing fields (title, date, solved flag, suspect, photo).
  - Handles interactions like picking a date, taking a photo, selecting a contact, and sharing.

On phones, navigation is typically:

- `CrimeListFragment/Activity` → `CrimeDetailFragment/Activity` and back.

On larger devices (e.g., tablets), this can evolve toward a **two-pane layout**, where:

- The list and detail screens appear side-by-side.
- Selecting a crime updates the detail pane dynamically.

This structure supports:

- Clear separation of responsibilities.
- Easier navigation management.
- Straightforward scaling to multiple layouts and device sizes.

---

## 5. Major Components

- **Data Model**
  - `Crime` data class, including:
    - Unique ID
    - Title
    - Date
    - Solved flag
    - Suspect name
    - Photo filename/URI (if applicable)
  - Repository or data layer for:
    - In-memory or persistent storage (e.g., database in later phases).
    - CRUD operations on crime records.
    - Providing observable data streams to UI components.

- **List Screen**
  - `CrimeListFragment` (or equivalent):
    - Displays the list of crimes.
    - Uses an adapter-backed RecyclerView for efficient scrolling.
    - Exposes “Add crime” action (e.g., toolbar or FAB).
    - Navigates to the detail screen for the selected crime.
  - Handles UI state restoration and efficient updates as crimes change.

- **Detail Screen**
  - `CrimeDetailFragment` (or equivalent):
    - Shows and edits a single crime’s fields.
    - Integrates date picker, camera/photo capture, contacts picker, and share intents.
    - Saves updates back through the repository or ViewModel.

- **Image Handling**
  - Utilities and permissions for camera access and file storage.
  - Logic to associate an image with the correct crime record (e.g., via filename or URI).
  - Image loading and display (scaled appropriately to avoid memory issues).

- **Contact Integration**
  - Use of Android’s Contacts provider.
  - Intent to pick a contact for suspect selection.
  - Retrieval and display of contact name.

- **Sharing Functionality**
  - Report construction:
    - Title, date, solved status.
    - Suspect name (or placeholder like “no suspect”).
  - Implicit sharing intent:
    - Text payload for email/social apps.
    - Let Android present an app chooser.

---

## 6. Scalability & Multi-Phase Development

CriminalIntent is intentionally designed to grow across **approximately 13 chapters/phases**. Each phase is expected to:

- Introduce new features (e.g., photos, contacts, database persistence).
- Improve the architecture (e.g., introducing ViewModels, LiveData/Flow, repositories).
- Refine UI and UX progressively.

To support this:

- **Modular design**
  - Clear separation between UI, domain logic, and data/storage.
  - Encapsulated components (fragments, adapters, helpers).

- **Extensibility**
  - Data model designed to easily accept new fields (e.g., location, severity, tags).
  - Navigation structure that can handle additional screens or flows.
  - Room for adding tests, internationalization, and accessibility improvements.

- **Maintainability**
  - Consistent naming and package structure.
  - Single-responsibility classes and functions.
  - Reusable UI components and utility classes (for images, dates, intents, etc.).

- **User-friendly design**
  - Simple, predictable navigation.
  - Clear affordances for adding/editing crimes.
  - Feedback and visual cues for important actions (e.g., share, photo capture).
  - Layouts that adapt gracefully to different screen sizes and orientations.

---

## 7. Design Principles & Expectations

- **Clean architecture**
  - UI layer (activities/fragments).
  - Domain/model layer (crime entities and business logic).
  - Data layer (repository, storage, later database).
  - Minimal coupling between layers.

- **Robustness**
  - Handle configuration changes (rotation, process death) gracefully.
  - Preserve user edits across lifecycle events as development progresses.

- **Delightful experience**
  - Humor in content and copy, professionalism in execution.
  - Quick interactions, responsive UI, and intuitive behaviors.

---

### Summary

CriminalIntent is a playful yet well-architected, scalable list–detail Android app. It lets users create, edit, and share “office crime” records with titles, dates, photos, suspects from contacts, and shareable reports, while emphasizing clean architecture, maintainability, and a user-friendly, evolving design over multiple development phases.
