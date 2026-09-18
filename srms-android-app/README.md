# SRMS Android App
**BINYAMINU USMAN POLYTECHNIC, HADEJIA — Student Results Management System**

A native Kotlin Android app that talks to the same PHP/MySQL backend as the
website (see `../srms/`), via a JSON REST API under `/srms/api/`. Both the
website and this app share one database — a student or admin can use either.

## What's inside

**Student screens:** Login, Sign Up, Profile (mandatory before anything
else unlocks), Dashboard, Course Registration (live countdown + Remita-style
reference generation against Access account **1646445316**), Upload Project
(automatic duplicate detection), Results (with GPA, downloadable as PDF via
Android's built-in print service), Course Materials (download).

**Admin screens:** Login, Dashboard (live stats), Students list + full
student detail (profile, registrations, results, projects, suspend/reactivate),
Courses (add/delete, categorized by Session → Semester → Level), Registration
Window (open/close countdown control with native date/time pickers), Upload
Results, Teaching Materials (upload/delete), Student Projects (all /
duplicate-flagged filter).

## Requirements

- Android Studio (Koala or newer recommended)
- The PHP backend from `../srms/` running on XAMPP, with the database
  imported and an admin account created (see that folder's own README)

## Setup

1. **Open the project**: In Android Studio, `File → Open`, select the
   `SRMS-Android` folder. Let Gradle sync — on first sync it will download
   the Gradle wrapper distribution automatically (requires internet).
   > If Android Studio complains about a missing wrapper jar, click
   > "Try Again" after it prompts to regenerate it, or run
   > `gradle wrapper --gradle-version 8.6` once from a terminal if you have
   > a system Gradle install.

2. **Start XAMPP** (Apache + MySQL) and make sure the website works first —
   visit `http://localhost/srms/` in a browser to confirm.

3. **Run the app**:
   - **Emulator**: just run the app — it defaults to
     `http://10.0.2.2/srms/api/`, which is the special alias emulators use
     to reach `localhost` on your development machine. No changes needed.
   - **Real device**: it must be on the same Wi-Fi network as your PC.
     Find your PC's local IP (e.g. `192.168.1.20`, via `ipconfig` /
     `ifconfig`), then in the app go to **Role Select → ⚙ Server Settings**
     and enter `http://192.168.1.20/srms/api/`.

4. **Sign in**:
   - Student: use "Sign up" the first time, then complete your profile —
     the app won't let you register courses or use most features until
     that's done, matching the website's behavior.
   - Admin: use the account you created via `setup.php` on the web side.

## How the pieces fit together

- `../srms/api/*.php` — new JSON endpoints added alongside the existing
  website pages. They reuse the same `config/db.php`, `config/functions.php`,
  and database tables as the website.
- `students` / `admins` tables gained an `api_token` column (see
  `../srms/sql/schema.sql` or `migration_add_api_token.sql` if you already
  had the database set up) — the app authenticates with a bearer token
  instead of PHP sessions/cookies.
- File uploads (project files, teaching materials, profile photos) are sent
  as `multipart/form-data` and stored in the same `uploads/` folders the
  website already uses — a file uploaded from the app shows up on the
  website and vice versa.

## Known limitations / things to harden further

- The Remita flow is a simulated reference generator, exactly like the
  website — no real payment gateway is involved.
- `network_security_config.xml` allows cleartext (plain HTTP) traffic,
  which is fine for local development on XAMPP but should be replaced with
  HTTPS before any real deployment.
- Tokens don't expire server-side; for production you'd want to add token
  expiry/refresh and possibly rate-limiting on the login endpoints.
- The app hasn't been run through a full Gradle build in this environment
  (no Android SDK/network access here) — if you hit a compile error when
  you open it in Android Studio, most likely culprits are a stray import or
  a view ID typo; every screen follows the same ViewBinding pattern, so
  errors are usually easy to trace to one activity/layout pair.
