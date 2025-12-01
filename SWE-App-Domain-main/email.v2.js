// email.v2.js
(function () {
  const pageRole = (document.documentElement.getAttribute("data-role") || "").toLowerCase();

  const KEY = "sf_mail_v2";
  function loadMail() { try { return JSON.parse(localStorage.getItem(KEY) || "[]"); } catch { return []; } }
  function saveMail(all) { localStorage.setItem(KEY, JSON.stringify(all)); }

  // message: { id, from, to, subject, body, dateISO, folder: 'inbox'|'spam'|'trash', tags?:[] }
  function send({ from, to, subject, body }) {
    const all = loadMail();
    const now = new Date().toISOString();
    const msg = {
      id: crypto.randomUUID(),
      from, to, subject, body,
      dateISO: now,
      folder: "inbox",
      tags: []
    };
    all.unshift(msg);
    saveMail(all);
    return msg;
  }

  // --- elements
  const $ = (s,r=document)=>r.querySelector(s);
  const $$ = (s,r=document)=>Array.from(r.querySelectorAll(s));
  const mailList = $("#mailList");
  const mailView = $("#mailView");
  const railBtns = $$(".rail-btn");
  const countInbox = $("#countInbox");
  const countSpam  = $("#countSpam");
  const countTrash = $("#countTrash");
  const searchInput = $("#searchInput");

  const composeBtn = $("#composeBtn");
  const drawer = $("#composeDrawer");
  const closeCompose = $("#closeCompose");
  const form = $("#composeForm");
  const toRoleSel = $("#toRole");
  const subjectEl = $("#subject");
  const bodyEl = $("#body");

  let activeFolder = "inbox";
  let activeId = null;
  let query = "";

  function fmtDate(iso){ return new Date(iso).toLocaleDateString(undefined,{month:"short",day:"numeric",year:"numeric"}); }

  function inboxFor(role){ return loadMail().filter(m => m.to === role && m.folder === "inbox"); }
  function spamFor(role){ return loadMail().filter(m => m.to === role && m.folder === "spam"); }
  function trashFor(role){ return loadMail().filter(m => m.to === role && m.folder === "trash"); }

  function updateCounts(){
    countInbox.textContent = inboxFor(pageRole).length;
    countSpam.textContent  = spamFor(pageRole).length;
    countTrash.textContent = trashFor(pageRole).length;
  }

  function filteredItems(){
    const all = loadMail().filter(m => m.to === pageRole && m.folder === activeFolder);
    if (!query) return all;
    const q = query.toLowerCase();
    return all.filter(m =>
      (m.from||"").toLowerCase().includes(q) ||
      (m.subject||"").toLowerCase().includes(q) ||
      (m.body||"").toLowerCase().includes(q)
    );
  }

  function renderList(){
    const items = filteredItems();
    mailList.innerHTML = items.map(m => `
      <div class="mail2-row" data-id="${m.id}">
        <div class="mail2-from">
          <div class="avatar avatar-dot"></div>
          <span>${capitalize(m.from)}</span>
        </div>
        <div class="mail2-mid">
          <span class="mail2-subject">${escapeHTML(m.subject || "(no subject)")}</span>
          <span class="mail2-snippet"> ${escapeHTML((m.body||"").slice(0,60))}${(m.body||"").length>60?"…":""}</span>
          <span class="mail2-badges"></span>
        </div>
        <div class="mail2-date">${fmtDate(m.dateISO)}</div>
      </div>
    `).join("") || `<div class="empty-state">No emails in ${capitalize(activeFolder)}.</div>`;

    $$(".mail2-row", mailList).forEach(row=>{
      row.addEventListener("click", ()=>{
        activeId = row.dataset.id;
        $$(".mail2-row", mailList).forEach(r=>r.classList.toggle("is-selected", r.dataset.id===activeId));
        openMessage(items.find(x=>x.id===activeId));
      });
    });

    if (!activeId && items[0]) {
      // auto-select first like many mail apps
      const first = $$(".mail2-row", mailList)[0];
      first?.click();
    } else if (activeId && !items.find(x=>x.id===activeId)) {
      // selected msg not in filtered set anymore
      mailView.innerHTML = `<div class="empty-state">Select an email to read</div>`;
    }
  }

  function openMessage(m){
    if (!m) return;
    mailView.innerHTML = `
      <div class="read-head">
        <h2>${escapeHTML(m.subject || "(no subject)")}</h2>
        <div class="read-meta">
          <span><strong>From:</strong> ${capitalize(m.from)}</span>
          <span><strong>To:</strong> ${capitalize(m.to)}</span>
          <span>${fmtDate(m.dateISO)}</span>
        </div>
        <div class="read-actions">
          ${m.folder!=="spam"?`<button class="mini" id="toSpam">Mark Spam</button>`:`<button class="mini" id="unSpam">Not Spam</button>`}
          ${m.folder!=="trash"?`<button class="mini" id="toTrash">Move to Trash</button>`:`<button class="mini" id="restore">Restore</button>`}
          <button class="primary" id="replyBtn">Reply</button>
        </div>
      </div>
      <div class="read-body">${escapeHTML(m.body || "")}</div>
    `;

    $("#replyBtn")?.addEventListener("click", ()=>{
      openCompose(m.from, `Re: ${m.subject||""}`, `\n\n--- On ${new Date(m.dateISO).toLocaleString()} ${capitalize(m.from)} wrote ---\n${m.body||""}\n`);
    });

    $("#toSpam")?.addEventListener("click", ()=> moveFolder(m.id,"spam"));
    $("#unSpam")?.addEventListener("click", ()=> moveFolder(m.id,"inbox"));
    $("#toTrash")?.addEventListener("click", ()=> moveFolder(m.id,"trash"));
    $("#restore")?.addEventListener("click",   ()=> moveFolder(m.id,"inbox"));
  }

  function moveFolder(id, folder){
    const all = loadMail();
    const idx = all.findIndex(x=>x.id===id);
    if (idx>-1) {
      all[idx].folder = folder;
      saveMail(all);
      activeId = id;
      updateCounts();
      renderList();
    }
  }

  // Compose
  function openCompose(to="", subject="", body=""){
    if (to) toRoleSel.value = to;
    subjectEl.value = subject || "";
    bodyEl.value = body || "";
    drawer.classList.add("open");
    drawer.setAttribute("aria-hidden","false");
    subjectEl.focus();
  }
  function closeComposer(){
    drawer.classList.remove("open");
    drawer.setAttribute("aria-hidden","true");
  }
  composeBtn?.addEventListener("click", ()=>openCompose());
  closeCompose?.addEventListener("click", closeComposer);
  form?.addEventListener("submit", (e)=>{
    e.preventDefault();
    const to = toRoleSel.value;
    const subject = subjectEl.value.trim();
    const body = bodyEl.value.trim();
    send({ from: pageRole, to, subject, body });
    closeComposer();
    updateCounts();
    // stay in current folder; user will see it in recipient's Inbox
  });

  // Search
  searchInput?.addEventListener("input", ()=>{
    query = searchInput.value || "";
    renderList();
  });

  // Rail switching
  railBtns.forEach(btn=>{
    btn.addEventListener("click", ()=>{
      railBtns.forEach(b=>b.classList.remove("is-active"));
      btn.classList.add("is-active");
      activeFolder = btn.dataset.folder;
      activeId = null;
      renderList();
    });
  });

  // Deep-link compose
  (function prefillFromQuery(){
    const p = new URLSearchParams(location.search);
    if (p.get("compose")==="1"){
      openCompose(p.get("to")||"", p.get("subject")||"", p.get("body")||"");
    }
  })();

  // Helpers
  function capitalize(s){ return (s||"").slice(0,1).toUpperCase()+ (s||"").slice(1); }
  function escapeHTML(s){
    return (s||"").replace(/[&<>"']/g, c=>({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;" }[c]));
  }

  // seed counts and render
  updateCounts();
  renderList();
})();
