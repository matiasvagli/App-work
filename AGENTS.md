# Repository Instructions

## Code Organization

- Prefer keeping source files under 400 lines when adding new code or making meaningful edits.
- Treat the 400-line limit as a maintainability guideline, not a hard rule. Do not split an existing larger file just to satisfy the number if it is working well, cohesive, and a refactor would add risk without clear value.
- When touching a file that already exceeds 400 lines, avoid making it substantially larger unless the change is tightly related. If the added behavior is separable, prefer extracting it into a focused helper, model, or component.

## Git Workflow

- Create small, descriptive commits after each validated change when committing is appropriate.
- Do not add `Co-authored-by` lines to commits.
- Do not change global or local Git configuration.
- Use the Git identity already configured in this repository.
- Do not push without explicit authorization from the user.
