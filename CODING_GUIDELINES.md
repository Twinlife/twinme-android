Coding rules for the project:

In the rule descriptions, `should` means this is a strong recommendation but we could have
documented exception, `must` and `shall` both mean it is mandatory.

## 1. General Principles

These rules also apply to submodules:

- Code must be Java 8 compatible: no `var`, no records, no newer language features.
- Files start with the twinlife copyright header and the `SPDX-License-Identifier: AGPL-3.0-only`
  line, followed by the contributors list in the form `<full name> (<email-address>)`:

  ```java
  /*
   *  Copyright (c) 2015-2026 twinlife SA.
   *  SPDX-License-Identifier: AGPL-3.0-only
   *
   *  Contributors:
   *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
   *   Stephane Carrez (Stephane.Carrez@twin.life)
   */
  ```

- Human contributors are mentioned when they submit significant changes or bug fixes,
  AI are excluded from the contributors list
- The copyright year shall be updated to indicate the last year the file was changed:
  a new file uses a single year (`2026`), a file created in an earlier year uses a
  range `<creation year>-<last modification year>` (`2015-2026`).
- Fields must use the `m` prefix and are `private` unless a subclass needs them.
- Static fields should use the `s` prefix and be `private` as much as possible.
- Fields and parameters must be annotated with `@NonNull` / `@Nullable` when they hold references.
- Inputs received in the constructor must be `final` (unless there is a real good reason not to be).
- Implementation should follow standard Java coding styles.
- Before a class-cast, there should be an `instanceof` check.
- Consecutive calls to retrieve the same value should be avoided and a `final` variable
  should be introduced if necessary.
- A `volatile` field must be copied to a `final` local variable before accessing it
  (unless it is returned by a function).
- Each method should have a call to `Log.d()` guarded by `if (DEBUG)` with a static final
  `DEBUG` and a `LOG_TAG` representing the class name; the log message should give hints
  for debugging calls.  Simple getter may not follow this rule.
- Use `androidx.annotation` for annotations (even if the code is expected to run on a server).
- When working on a sub-module, read and take into account the module `CODING_GUIDELINES.md`.

## 2. Android Activity implementation rules

- Android activity classes should inherit from `AbstractTwinmeActivity`.
- inherited methods `onCreate()`, `onPause()`, `onResume()`,
  `onDestroy()`, `onConfigurationChanged()`, must call the super method.

## 3. Protocols implementation

- For a protocol serialization implementation, follow our Encoder/Decoder architecture
  defined in Java package `org.twinlife.twinlife` and available in the
  files `twinlife-framework/src/main/java/org/twinlife/twinlife/Encoder.java`
  and `twinlife-framework/src/main/java/org/twinlife/twinlife/Decoder.java`.
  Example of such serialization in twinlife-framework/src/main/java/org/twinlife/twinlife/secureroster/OnListRosterIQ.java
- For the protocol, each packet is described by a Java class whose name ends with `IQ`.
  The Java class contains the members that must be serialized.
  It also contains inner static classes to implement the serialization based on the
  Encoder or Decoder interfaces.
- Each packet inherits from BinaryPacketIQ and therefore is associated with a unique
  schema ID (a UUID) and a schema version.

## 4. State machines

State machine classes are used for the executors in `twinme-framework` and the
UI service helper classes in `twinme-android-common`.  They follow these rules:

- `onOperation()` is a re-entrant step state machine: it must be idempotent and
  re-entrant: it is called on start, and it is called again by every asynchronous
  callback to make progress, so it must be able to run several times and only
  trigger each step once.
- the state machine progress must be tracked by a `private|protected int mState = 0;` with
  bit-flag constants declared in pairs, one to mark that a step has been started
  and one to mark that its result arrived.
- some state machine steps may be tracked by a single bit-flag constant when their
  execution does not produce an asynchronous result.
- Each step follows the same template: set the started bit before calling the service
  so a re-entry cannot issue the call twice, then return while the done bit is not set.
- Asynchronous callback steps shall be implemented as `protected` or `private` method
  with a `on` prefix and shall get the error code and result value (ex:
  `onGetTwincodeOutbound()`, `onSendFeedback()`, ...), it shall verify the error code
  and the result (if any), calls `onOperationError()/onError()` and return if there was an error,
  or, the method shall set the `_DONE` bit and proceed with the next step by calling
  `onOperation()`.
- when an inherited method `onTwinlifeReady()`, `onTwinlifeOnline()` is overridden,
  it must call the super method.
