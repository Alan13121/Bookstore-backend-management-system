const apiBase = "/api/roles/mappings";

const headers = authHeader();

document.getElementById("roleForm").addEventListener("submit", async (e) => {
  e.preventDefault();

  const urlPattern = document.getElementById("urlPattern").value.trim();
  const roles = document.getElementById("roles").value.trim();

  if (!urlPattern || !roles) {
    alert("請完整輸入！");
    return;
  }

  const res = await fetch(apiBase, {
    method: "POST",
    headers,
    body: JSON.stringify({ urlPattern, roles })
  });

  if (res.ok) {
    alert("✅ 新增成功");
    document.getElementById("roleForm").reset();
    loadRules();
  } else {
    alert("❌ 新增失敗");
  }
});

async function loadRules() {
  const res = await fetch(apiBase, { headers });
  if (!res.ok) {
    alert("❌ 載入規則失敗");
    return;
  }

  const data = await res.json();

  const list = document.getElementById("rulesList");
  list.innerHTML = "";

  data.forEach(rule => {
    const li = document.createElement("li");
    li.innerHTML = `
      <input value="${rule.urlPattern}" id="url-${rule.id}">
      <input value="${rule.roles}" id="roles-${rule.id}">
      <button onclick="updateRule(${rule.id})">更新</button>
      <button onclick="deleteRule(${rule.id})">刪除</button>
    `;
    list.appendChild(li);
  });
}

async function deleteRule(id) {
  const res = await fetch(`${apiBase}/${id}`, {
    method: "DELETE",
    headers
  });
  if (res.ok) {
    alert("✅ 刪除成功");
    loadRules();
  } else {
    alert("❌ 刪除失敗");
  }
}

async function updateRule(id) {
  const urlPattern = document.getElementById(`url-${id}`).value.trim();
  const roles = document.getElementById(`roles-${id}`).value.trim();

  if (!urlPattern || !roles) {
    alert("請完整輸入！");
    return;
  }

  const res = await fetch(`${apiBase}/${id}`, {
    method: "PUT",
    headers,
    body: JSON.stringify({ urlPattern, roles })
  });

  if (res.ok) {
    alert("✅ 更新成功");
    loadRules();
  } else {
    alert("❌ 更新失敗");
  }
}

// 頁面載入時
loadRules();




const roleApiBase = "/api/roles";
const roleHeaders = authHeader();

// 新增角色
document.getElementById("roleForm2").addEventListener("submit", async (e) => {
  e.preventDefault();

  const name = document.getElementById("roleName").value.trim();
  if (!name) {
    alert("請輸入角色名稱！");
    return;
  }

  const res = await fetch(roleApiBase, {
    method: "POST",
    headers: roleHeaders,
    body: JSON.stringify({ name })
  });

  if (res.ok) {
    alert("✅ 新增角色成功");
    document.getElementById("roleForm2").reset();
    loadRoles();
  } else {
    alert("❌ 新增角色失敗");
  }
});

// 載入角色列表
async function loadRoles() {
  const res = await fetch(roleApiBase, { headers: roleHeaders });
  if (!res.ok) {
    alert("❌ 載入角色失敗");
    return;
  }

  const data = await res.json();

  const list = document.getElementById("rolesList");
  list.innerHTML = "";

  data.forEach(role => {
    const li = document.createElement("li");
    li.innerHTML = `
      <input value="${role.name}" id="name-${role.id}">
      <button onclick="updateRole(${role.id})">更新</button>
      <button onclick="deleteRole(${role.id})">刪除</button>
    `;
    list.appendChild(li);
  });
}

// 刪除角色
async function deleteRole(id) {
  const res = await fetch(`${roleApiBase}/${id}`, {
    method: "DELETE",
    headers: roleHeaders
  });
  if (res.ok) {
    alert("✅ 刪除角色成功");
    loadRoles();
  } else {
    alert("❌ 刪除角色失敗");
  }
}

// 更新角色
async function updateRole(id) {
  const name = document.getElementById(`name-${id}`).value.trim();
  if (!name) {
    alert("請輸入角色名稱！");
    return;
  }

  const res = await fetch(`${roleApiBase}/${id}`, {
    method: "PUT",
    headers: roleHeaders,
    body: JSON.stringify({ name })
  });
  if (res.ok) {
    alert("✅ 更新角色成功");
    loadRoles();
  } else {
    alert("❌ 更新角色失敗");
  }
}

// 頁面載入時先載入角色
loadRoles();
