# 05 - CI/CD Guide

This document describes the automated CI/CD pipeline and branch policies configured for OralCare.

---

## 1. GitHub Actions Pipeline
The `.github/workflows/ci-cd.yml` workflow coordinates builds, checks, and deployments:
1. **Compilation**: Verifies JDK 17 compilation paths.
2. **Testing**: Spins up Postgres containers using Testcontainers and runs all unit and integration tests.
3. **Docker Build**: Compiles a production-grade multi-stage image.
4. **Push**: Pushes the image to GitHub Container Registry (GHCR) with standard version and SHA tags.
5. **Staging Rollout**: Triggers a simulated CD deployment step on updates to the `develop` branch.

---

## 2. Branching & PR Strategy
- **`main`**: Production release branch. All code must enter via PRs from `develop` with code owner approvals and passing CI statuses.
- **`develop`**: Integration and staging branch. Feature branches merge here after local verification.
- **`feature/*`**: Topic branches. Tested and code-reviewed before being merged into `develop`.
- **`hotfix/*`**: Immediate production patches. Merged into both `main` and `develop`.
