# Commit Decision History

> 此文件是 `commits.jsonl` 的人类可读视图，可由工具重生成。
> Canonical store: `commits.jsonl` (JSONL, append-only)

| Date | Context-Id | Commit | Summary | Decisions | Bugs | Risk |
|------|-----------|--------|---------|-----------|------|------|
| 2026-03-27T17:40:33+08:00 | `1eaf11d0-0bd5-4bc9-9b10-97b8ab56b809` | - | chore(project): 初始化上下文文档并切换后端默认环境 | 初始化 .context 目录结构并提交团队共享的开发规范与上下文历史文件。<br>补充根级与前后端模块级 CLAUDE.md 作为项目索引文档。<br>将 backend 默认激活 profile 切换为 prod。 | - | - |
| 2026-03-27T17:41:45+08:00 | `98d3175d-3414-4c8c-8237-df43ff62b0a8` | - | feat(outbound): 支持按状态查看并导出出库取消记录 | 为出库记录增加 status 与 cancelTime 字段，用状态标记替代取消时直接删除记录。<br>后端查询与导出接口增加 status 条件，支持正常记录与取消记录分视图查询。<br>前端为设备类与耗材类新增出库取消记录路由，并在取消记录页禁用新增与取消操作。 | - | - |
