# Designing Amazon Locker

## Requirements
1. The system should manage a bank of lockers at a location, each locker having a size (SMALL, MEDIUM, LARGE).
2. When a package arrives for drop-off, the system should assign it the smallest available locker that fits the package's size.
3. On successful assignment, the system should generate a unique access code and notify the customer (e.g. via email/SMS) with the code and locker location.
4. A locker that is occupied should not be assignable to another package until it is vacated.
5. The customer should be able to open the locker by presenting the correct access code; an incorrect code should be rejected.
6. Once the customer retrieves the package, the locker should be released and become available for new assignments.
7. If a package is not picked up within a configurable expiry window, the system should expire the access code and free the locker (e.g. return package to carrier).
8. The system should support multiple locker locations, each with its own independent bank of lockers.
9. The system should handle concurrent drop-offs and pickups without double-assigning the same locker.
10. The system should be extensible to support notification channels beyond email/SMS (push, app notification) without changing core assignment logic.

## Out of scope (for now)
- Payment/billing for locker usage.
- Physical hardware integration (assume a software-only simulation of open/close).
