const { execSync } = require('child_process');

// 获取环境变量
const projectId = process.env.PROJECT_ID || 'PVT_kwHOCoF7pM4BFUrC';
const commitTitle = process.env.COMMIT_TITLE;
const commitAuthor = process.env.COMMIT_AUTHOR;
const commitDate = process.env.COMMIT_DATE;
const commitAuthorEmail = process.env.COMMIT_AUTHOR_EMAIL;
const repoOwner = process.env.REPO_OWNER;
const repoName = process.env.REPO_NAME;
const commitSha = process.env.COMMIT_SHA;
const commitUrl = process.env.COMMIT_URL;

async function syncCommitToProject() {
    try {
        console.log('开始同步提交到项目...');
        console.log(`提交标题: ${commitTitle}`);
        console.log(`提交作者: ${commitAuthor}`);
        console.log(`提交日期: ${commitDate}`);

        // 创建 Issue
        console.log('创建 Issue...');
        const issue = await github.rest.issues.create({
            owner: repoOwner,
            repo: repoName,
            title: commitTitle,
            body: `Author: ${commitAuthor}\n\nCommit: ${commitSha}\n\n[查看提交](${commitUrl})`,
            labels: ['project-sync', 'auto-generated']
        });

        console.log(`Issue 创建成功: ${issue.data.html_url}`);

        // 添加 Issue 到 ProjectV2
        console.log('添加 Issue 到项目...');
        const addItemResult = await github.graphql(`
      mutation {
        addProjectV2ItemById(input: {
          projectId: "${projectId}",
          contentId: "${issue.data.node_id}"
        }) {
          item {
            id
          }
        }
      }
    `);

        const projectItemId = addItemResult.addProjectV2ItemById.item.id;
        console.log(`项目项 ID: ${projectItemId}`);

        // 获取项目字段信息
        console.log('获取项目字段信息...');
        const projectFields = await github.graphql(`
      query {
        node(id: "${projectId}") {
          ... on ProjectV2 {
            fields(first: 20) {
              nodes {
                ... on ProjectV2Field {
                  id
                  name
                }
                ... on ProjectV2SingleSelectField {
                  id
                  name
                  options {
                    id
                    name
                  }
                }
              }
            }
          }
        }
      }
    `);

        const fields = projectFields.node.fields.nodes;
        let statusFieldId = null;
        let assigneesFieldId = null;
        let startDateFieldId = null;
        let inProgressOptionId = null;

        // 查找字段 ID
        console.log('查找项目字段...');
        for (const field of fields) {
            if (field.name === 'Status') {
                statusFieldId = field.id;
                console.log(`找到 Status 字段: ${field.id}`);
                // 查找 "In progress" 选项
                if (field.options) {
                    const inProgressOption = field.options.find(option =>
                        option.name === 'In progress' || option.name === 'In Progress'
                    );
                    if (inProgressOption) {
                        inProgressOptionId = inProgressOption.id;
                        console.log(`找到 "In progress" 选项: ${inProgressOptionId}`);
                    }
                }
            } else if (field.name === 'Assignees') {
                assigneesFieldId = field.id;
                console.log(`找到 Assignees 字段: ${field.id}`);
            } else if (field.name === 'Start date') {
                startDateFieldId = field.id;
                console.log(`找到 Start date 字段: ${field.id}`);
            }
        }

        // 设置 Status 为 "In progress"
        if (statusFieldId && inProgressOptionId) {
            console.log('设置 Status 为 "In progress"...');
            await github.graphql(`
        mutation {
          updateProjectV2ItemFieldValue(input: {
            projectId: "${projectId}",
            itemId: "${projectItemId}",
            fieldId: "${statusFieldId}",
            value: {
              singleSelectOptionId: "${inProgressOptionId}"
            }
          }) {
            projectV2Item {
              id
            }
          }
        }
      `);
            console.log('Status 设置成功');
        } else {
            console.log('未找到 Status 字段或 "In progress" 选项');
        }

        // 设置 Assignees 为提交者
        if (assigneesFieldId) {
            console.log('设置 Assignees 为提交者...');
            // 首先尝试通过用户名查找用户
            let assigneeId = null;
            try {
                const user = await github.rest.users.getByUsername({
                    username: commitAuthor
                });
                assigneeId = user.data.node_id;
                console.log(`找到用户: ${commitAuthor} (${assigneeId})`);
            } catch (error) {
                console.log(`无法找到用户: ${commitAuthor}`, error.message);
            }

            if (assigneeId) {
                await github.graphql(`
          mutation {
            updateProjectV2ItemFieldValue(input: {
              projectId: "${projectId}",
              itemId: "${projectItemId}",
              fieldId: "${assigneesFieldId}",
              value: {
                userIds: ["${assigneeId}"]
              }
            }) {
              projectV2Item {
                id
              }
            }
          }
        `);
                console.log('Assignees 设置成功');
            } else {
                console.log('无法设置 Assignees，用户不存在');
            }
        } else {
            console.log('未找到 Assignees 字段');
        }

        // 设置 Start date 为提交日期
        if (startDateFieldId) {
            console.log(`设置 Start date 为 ${commitDate}...`);
            await github.graphql(`
        mutation {
          updateProjectV2ItemFieldValue(input: {
            projectId: "${projectId}",
            itemId: "${projectItemId}",
            fieldId: "${startDateFieldId}",
            value: {
              date: "${commitDate}"
            }
          }) {
            projectV2Item {
              id
            }
          }
        }
      `);
            console.log('Start date 设置成功');
        } else {
            console.log('未找到 Start date 字段');
        }

        console.log('同步完成！');
        return {
            success: true,
            issueUrl: issue.data.html_url,
            projectItemId: projectItemId
        };

    } catch (error) {
        console.error('同步过程中发生错误:', error);
        throw error;
    }
}

// 如果直接运行此脚本
if (require.main === module) {
    syncCommitToProject()
        .then(result => {
            console.log('脚本执行成功:', result);
            process.exit(0);
        })
        .catch(error => {
            console.error('脚本执行失败:', error);
            process.exit(1);
        });
}

module.exports = { syncCommitToProject };
