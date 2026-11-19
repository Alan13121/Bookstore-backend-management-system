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
  try {
    renderBookTable(await apiFetch('/api/books'));
  } catch (err) {
    alert(`載入書籍失敗：${err.message}`);
  }
}

function renderBookTable(books) {
  const container = document.getElementById('book-table');
  if (!books || books.length === 0) {
    container.innerHTML = '<p>沒有書籍資料</p>';
    return;
  }

  // 使用者輸入的資料一律經過 escapeHtml，避免書名裡的 <script> 之類被當成 HTML 執行
  const rows = books
    .map(
      (book) => `
      <tr>
        <td>${escapeHtml(book.id)}</td>
        <td>${escapeHtml(book.title)}</td>
        <td>${escapeHtml(book.author)}</td>
        <td>${escapeHtml(book.description)}</td>
        <td>${escapeHtml(book.listPrice)}</td>
        <td>${escapeHtml(book.salePrice)}</td>
        <td>
          <button type="button" data-action="delete" data-id="${escapeHtml(book.id)}">刪除</button>
          <a href="/book/book.html?id=${encodeURIComponent(book.id)}">更改</a>
        </td>
      </tr>`,
    )
    .join('');

  container.innerHTML = `
    <table border="1">
      <thead>
        <tr>
          <th>ID</th><th>書名</th><th>作者</th><th>簡述</th><th>定價</th><th>售價</th><th>Action</th>
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
    alert('新增成功');
    document.getElementById('create-book-form').reset();
    getAllBooks();
  } catch (err) {
    alert(`新增失敗：${err.message}`);
  }
}

async function deleteBook(id) {
  if (!confirm(`確定要刪除書籍 ${id} 嗎?`)) return;
  try {
    await apiFetch(`/api/books/${id}`, { method: 'DELETE' });
    alert('刪除成功');
    getAllBooks();
  } catch (err) {
    alert(`刪除失敗：${err.message}`);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  if (!checkAuthOrRedirect()) return;

  getAllBooks();

  document.getElementById('create-book-form').addEventListener('submit', (e) => {
    e.preventDefault();
    createBook();
  });

  // 事件委派（event delegation）：表格每次重新渲染都會換掉裡面的按鈕，
  // 所以把監聽器掛在不會被換掉的外層容器上，再用 data-action 判斷點了哪個按鈕。
  document.getElementById('book-table').addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-action="delete"]');
    if (btn) deleteBook(btn.dataset.id);
  });
});
