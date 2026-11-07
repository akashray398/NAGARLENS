# NAGARLENS — CI/CD & Automated Quality Gates Guide

> **CI Engine:** GitHub Actions (`.github/workflows/production-release.yml`)  

---

## 1. Automated Workflow Jobs

1. **`backend-test-and-build`**: Runs Pytest unit and contract tests on Python 3.12.
2. **`android-test-and-build`**: Runs Android unit tests and compiles Debug/Release APKs on JDK 17.
