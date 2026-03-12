// Commitlint configuration for pet-admin frontend
// Commit format: <type>(<scope>): <subject>
// Example: feat(auth): add login page with JWT support

/** @type {import('@commitlint/types').UserConfig} */
export default {
    rules: {
        'type-enum': [
            2,
            'always',
            [
                'feat',     // ✨ Features|新功能
                'fix',      // 🐛 Bug Fixes|Bug 修复
                'init',     // 🎉 Init|初始化
                'docs',     // ✏️  Documentation|文档
                'style',    // 💄 Styles|风格
                'refactor', // ♻️  Refactoring|代码重构
                'perf',     // ⚡ Performance|性能优化
                'test',     // ✅ Tests|测试
                'revert',   // ⏪ Revert|回退
                'build',    // 📦 Build System|打包构建
                'chore',    // 🚀 Chore|工程依赖/工具
                'ci'        // 👷 CI|CI 配置
            ]
        ],
        'type-case': [2, 'always', 'lower-case'],
        'type-empty': [2, 'never'],
        'subject-empty': [2, 'never'],
        'subject-full-stop': [2, 'never', '.']
    }
};
