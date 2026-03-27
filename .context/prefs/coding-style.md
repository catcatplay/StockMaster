# Coding Style Guide

> 此文件定义团队编码规范，所有 LLM 工具在修改代码时必须遵守。
> 提交到 Git，团队共享。

## General
- Prefer small, reviewable changes; avoid unrelated refactors.
- Keep functions short (<50 lines); avoid deep nesting (≤3 levels).
- Name things explicitly; no single-letter variables except loop counters.
- Handle errors explicitly; never swallow errors silently.

## Language-Specific

### Java (Spring Boot)
- Use `@RestControllerAdvice` for global exception handling, not per-method try-catch.
- Use `BusinessException` for business logic errors.
- Inject beans via `@Autowired` / `@Resource`, never manually `new` Spring-managed objects.
- Follow existing package conventions: `controller/`, `service/`, `entity/`, `dto/`, `config/`, `exception/`, `util/`.

### Vue 3 (Composition API)
- All components use `<script setup>` syntax.
- Extract shared logic to `composables/` (e.g., `useExport`, `usePagination`).
- Extract shared utility functions to `utils/` (e.g., `date.js`).
- Use `reactive()` for pagination state, `ref()` for simple values.
- Use Element Plus components consistently.

## Git Commits
- Conventional Commits, imperative mood.
- Atomic commits: one logical change per commit.

## Testing
- Every feat/fix MUST include corresponding tests.
- Coverage must not decrease.
- Fix flow: write failing test FIRST, then fix code.

## Security
- Never log secrets (tokens/keys/cookies/JWT).
- Validate inputs at trust boundaries.
