// 書籍列表頁：列出、新增、刪除書籍（編輯在 book.html）

// 新增表單的欄位 id，對應 BookCreateRequest 的欄位名稱
const BOOK_FORM_FIELDS = {
  title: 'book-title',
  author: 'book-author',
  description: 'book-desc',
  listPrice: 'book-list-price',
  salePrice: 'book-sale-price',
};

async function getAllBooks() {
  const container = document.getElementById('book-table');
  showLoading(container);
  try {
    renderBookTable(await apiFetch('/api/books'));
  } catch (err) {
    container.innerHTML = '<p class="text-danger mb-0">書籍載入失敗</p>';
    showToast(`載入書籍失敗：${err.message}`, 'danger');
  }
}

function renderBookTable(books) {
  const container = document.getElementById('book-table');
  if (!books || books.length === 0) {
    container.innerHTML = '<p class="text-secondary mb-0">目前沒有書籍資料</p>';
    return;
  }

  // 使用者輸入的資料一律經過 escapeHtml，避免書名裡的 <script> 之類被當成 HTML 執行
  const rows = books
    .map(
      (book) => `
      <tr>
        <td class="text-secondary">${escapeHtml(book.id)}</td>
        <td class="fw-semibold">${escapeHtml(book.title)}</td>
        <td>${escapeHtml(book.author)}</td>
        <td class="text-secondary small">${escapeHtml(book.description)}</td>
        <td class="text-price">${escapeHtml(book.listPrice)}</td>
        <td class="text-price">${escapeHtml(book.salePrice)}</td>
        <td class="text-end text-nowrap">
          <a class="btn btn-sm btn-outline-primary" href="/book/book.html?id=${encodeURIComponent(book.id)}">編輯</a>
          <button type="button" class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${escapeHtml(book.id)}" data-title="${escapeHtml(book.title)}">刪除</button>
        </td>
      </tr>`,
    )
    .join('');

  container.innerHTML = `
    <table class="table table-hover align-middle mb-0">
      <thead>
        <tr>
          <th>ID</th><th>書名</th><th>作者</th><th>簡述</th>
          <th class="text-end">定價</th><th class="text-end">售價</th><th class="text-end">操作</th>
        </tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>`;
}

async function createBook() {
  const fields = readFields(BOOK_FORM_FIELDS);
  const book = {
    ...fields,
    listPrice: toNumberOrNull(fields.listPrice),
    salePrice: toNumberOrNull(fields.salePrice),
  };

  try {
    await apiFetch('/api/books', { method: 'POST', body: book });
    showToast(`已新增《${book.title}》`);
    document.getElementById('create-book-form').reset();
    getAllBooks();
  } catch (err) {
    showToast(`新增失敗：${err.message}`, 'danger');
  }
}

// 刪除按鈕帶有 data-id 與 data-title（title 用來在確認對話框顯示刪的是哪一本）
async function deleteBook(btn) {
  // dataset 讀的是 data-* 屬性：data-title → dataset.title
  const { id, title } = btn.dataset;
  const ok = await confirmDialog(`確定要刪除《${title}》嗎？刪除後無法復原。`, {
    title: '刪除書籍',
    confirmText: '刪除',
  });
  if (!ok) return;
  // 使用者確認後才讓按鈕進入載入中；若在確認前就轉圈，取消時畫面會很奇怪
  await withLoading(btn, async () => {
    try {
      await apiFetch(`/api/books/${id}`, { method: 'DELETE' });
      showToast(`已刪除《${title}》`);
      getAllBooks();
    } catch (err) {
      showToast(`刪除失敗：${err.message}`, 'danger');
    }
  });
}

document.addEventListener('DOMContentLoaded', () => {
  if (!checkAuthOrRedirect()) return;

  getAllBooks();

  // 驗證、防連點、按鈕載入中都由 common.js 的 onFormSubmit 處理
  onFormSubmit(document.getElementById('create-book-form'), createBook);

  // 事件委派（event delegation）：表格每次重新渲染都會換掉裡面的按鈕，
  // 所以把監聽器掛在不會被換掉的外層容器上，再用 data-action 判斷點了哪個按鈕。
  document.getElementById('book-table').addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-action="delete"]');
    if (btn) deleteBook(btn);
  });
});
