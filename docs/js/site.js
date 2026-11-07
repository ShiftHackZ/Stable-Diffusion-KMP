(() => {
  const site = {
    name: "SDAI",
    author: "Dmitriy Moroz",
    email: "sdai@moroz.cc",
    startYear: 2023,
    links: {
      home: "index.html",
      faq: "faq.html",
      docs: "docs/",
      donate: "donate.html",
      privacy: "privacy.html",
      github: "https://github.com/ShiftHackZ/SDAI-KMP",
      company: "https://moroz.cc",
      telegram: "https://t.me/sdai_app",
      discord: "https://discord.gg/jzdR9m8Ves",
      fourPda: "https://4pda.to/forum/index.php?showtopic=1082639",
      nightly: "nightly.html",
      testFlight: "testflight.html",
      googlePlay:
        "https://play.google.com/store/apps/details?id=com.shifthackz.aisdv1.app",
      fdroid: "https://f-droid.org/packages/com.shifthackz.aisdv1.app.foss",
      appStore:
        "https://apps.apple.com/us/app/sdai-ai-image-generator/id6778314183",
    },
  };

  const icons = {
    telegram:
      '<svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M21.86 4.58 18.62 19.84c-.24 1.08-.88 1.34-1.78.83l-4.92-3.63-2.37 2.28c-.26.26-.48.48-.98.48l.35-5.02 9.14-8.26c.4-.35-.09-.55-.61-.2L6.15 13.43l-4.87-1.52c-1.06-.33-1.08-1.06.22-1.57L20.55 3c.88-.33 1.65.2 1.31 1.58Z"/></svg>',
    discord:
      '<svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M19.54 5.26A18.1 18.1 0 0 0 15.04 3.86c-.19.34-.42.8-.57 1.16a16.7 16.7 0 0 0-4.99 0 12.3 12.3 0 0 0-.58-1.16 18 18 0 0 0-4.5 1.4C1.55 9.49.78 13.61 1.16 17.68a18.3 18.3 0 0 0 5.51 2.79c.44-.6.84-1.24 1.17-1.91-.64-.24-1.25-.54-1.82-.89.15-.11.3-.23.44-.35a12.96 12.96 0 0 0 11.08 0l.44.35c-.58.35-1.19.65-1.83.89.34.67.73 1.31 1.17 1.91a18.2 18.2 0 0 0 5.52-2.79c.46-4.72-.78-8.81-3.3-12.42ZM8.68 15.18c-1.07 0-1.95-.99-1.95-2.2 0-1.2.86-2.19 1.95-2.19 1.08 0 1.96.99 1.95 2.19 0 1.21-.87 2.2-1.95 2.2Zm6.64 0c-1.07 0-1.95-.99-1.95-2.2 0-1.2.86-2.19 1.95-2.19 1.09 0 1.96.99 1.95 2.19 0 1.21-.86 2.2-1.95 2.2Z"/></svg>',
    github:
      '<svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 2C6.48 2 2 6.58 2 12.25c0 4.52 2.87 8.36 6.84 9.72.5.09.68-.22.68-.49 0-.24-.01-1.05-.01-1.91-2.78.62-3.37-1.22-3.37-1.22-.45-1.19-1.11-1.51-1.11-1.51-.91-.64.07-.63.07-.63 1 .07 1.53 1.06 1.53 1.06.89 1.57 2.34 1.12 2.91.85.09-.66.35-1.12.64-1.38-2.22-.26-4.56-1.14-4.56-5.08 0-1.12.39-2.04 1.03-2.76-.1-.26-.45-1.31.1-2.72 0 0 .84-.28 2.75 1.05a9.37 9.37 0 0 1 5.01 0c1.91-1.33 2.75-1.05 2.75-1.05.55 1.41.2 2.46.1 2.72.64.72 1.03 1.64 1.03 2.76 0 3.95-2.34 4.82-4.57 5.08.36.32.68.94.68 1.9 0 1.37-.01 2.48-.01 2.82 0 .27.18.59.69.49A10.11 10.11 0 0 0 22 12.25C22 6.58 17.52 2 12 2Z"/></svg>',
  };

  const external = 'target="_blank" rel="noopener noreferrer"';

  const stableReleaseCta = {
    home: {
      label: "Get the app",
      title: "Start generating with SDAI today.",
      text: "Install from your preferred mobile app store or distribution channel and join the community for updates and help.",
    },
    stable: {
      label: "Latest stable release",
      title: "Get the stable build for everyday use.",
      text: "Install the current Google Play, F-Droid, or App Store release when you want the safest build. Keep beta channels for testing upcoming changes.",
    },
  };

  function setActiveNav() {
    const page = document.body.dataset.page;
    document.querySelectorAll("[data-nav]").forEach((link) => {
      if (link.dataset.nav === page) {
        link.setAttribute("aria-current", "page");
      }
    });
  }

  function renderHeader() {
    const mount = document.getElementById("site-header");
    if (!mount) return;

    mount.innerHTML = `
      <header class="site-header">
        <div class="shell nav-shell">
          <a class="brand" href="${site.links.home}" aria-label="${site.name} home">
            <img src="assets/sdai.png" width="42" height="42" alt="">
            <span>${site.name}</span>
          </a>
          <button class="menu-toggle" type="button" aria-label="Open navigation" aria-controls="site-menu" aria-expanded="false" data-menu-toggle>
            <span></span>
            <span></span>
            <span></span>
          </button>
          <div class="site-menu" id="site-menu" data-site-menu>
            <nav class="site-nav" aria-label="Main navigation">
              <a href="${site.links.home}" data-nav="home">Home</a>
              <a href="${site.links.faq}" data-nav="faq">FAQ</a>
              <a href="${site.links.privacy}" data-nav="privacy">Privacy Policy</a>
            </nav>
            <div class="social-links" aria-label="Community">
              <a class="icon-link" href="${site.links.telegram}" ${external} aria-label="Telegram">${icons.telegram}</a>
              <a class="icon-link" href="${site.links.discord}" ${external} aria-label="Discord">${icons.discord}</a>
            </div>
          </div>
        </div>
      </header>
    `;
  }

  function storeButtons() {
    const appStore = site.links.appStore
      ? `<a class="store-badge app-store-badge" href="${site.links.appStore}" ${external} aria-label="Download SDAI on the App Store">
          <img src="assets/badge-app-store.svg" width="162" height="54" alt="Download on the App Store">
        </a>`
      : `<span class="store-badge app-store-badge is-disabled" role="link" aria-disabled="true" title="App Store link is not available yet">
          <img src="assets/badge-app-store.svg" width="162" height="54" alt="Download on the App Store">
        </span>`;

    return `
      <a class="store-badge" href="${site.links.googlePlay}" ${external} aria-label="Get SDAI on Google Play">
        <img src="assets/badge-google-play.svg" width="182" height="54" alt="Get it on Google Play">
      </a>
      <a class="store-badge fdroid-badge" href="${site.links.fdroid}" ${external} aria-label="Get SDAI on F-Droid">
        <img src="assets/badge-fdroid.svg" width="184" height="54" alt="Get it on F-Droid">
      </a>
      ${appStore}
    `;
  }

  function footerAppStoreLink() {
    if (site.links.appStore) {
      return `<a href="${site.links.appStore}" ${external}>App Store</a>`;
    }
    return '<span class="footer-disabled" aria-disabled="true">App Store <span class="status-pill">Soon</span></span>';
  }

  function renderFooter() {
    const mount = document.getElementById("site-footer");
    if (!mount) return;

    mount.innerHTML = `
      <footer class="site-footer">
        <div class="shell footer-grid">
          <nav class="footer-column" aria-label="Info">
            <h2>Info</h2>
            <a href="${site.links.faq}">FAQ</a>
            <a href="${site.links.privacy}">Privacy Policy</a>
            <a href="${site.links.docs}">Documentation</a>
            <a href="${site.links.github}" ${external}>GitHub</a>
            <a href="${site.links.donate}">Donate</a>
          </nav>
          <nav class="footer-column" aria-label="Community">
            <h2>Community</h2>
            <a href="${site.links.telegram}" ${external}>Telegram</a>
            <a href="${site.links.discord}" ${external}>Discord</a>
            <a href="${site.links.fourPda}" ${external}>4PDA</a>
          </nav>
          <nav class="footer-column" aria-label="Beta testing">
            <h2>Beta Testing</h2>
            <a href="${site.links.nightly}">Nightly Build</a>
            <a href="${site.links.testFlight}">TestFlight Beta</a>
          </nav>
          <nav class="footer-column" aria-label="Get app">
            <h2>Get app</h2>
            <a href="${site.links.googlePlay}" ${external}>Google Play</a>
            <a href="${site.links.fdroid}" ${external}>F-Droid</a>
            ${footerAppStoreLink()}
          </nav>
          <div class="footer-brand">
            <a class="footer-brand-row" href="${site.links.home}" aria-label="${site.name} home">
              <img src="assets/sdai.png" width="38" height="38" alt="">
              <span>${site.name}</span>
            </a>
            <p class="copyright">
              <span><a href="mailto:${site.email}">${site.email}</a></span>
              <span>${site.name} App &copy; ${site.startYear} - <span data-year></span></span>
              <span>Developed by <a href="${site.links.company}" ${external}>Moroz Inc.</a></span>
            </p>
          </div>
        </div>
      </footer>
    `;
  }

  function initMenu() {
    const toggle = document.querySelector("[data-menu-toggle]");
    const menu = document.querySelector("[data-site-menu]");
    if (!toggle || !menu) return;

    const closeMenu = () => {
      menu.classList.remove("is-open");
      toggle.setAttribute("aria-expanded", "false");
      toggle.setAttribute("aria-label", "Open navigation");
    };

    toggle.addEventListener("click", () => {
      const nextState = toggle.getAttribute("aria-expanded") !== "true";
      menu.classList.toggle("is-open", nextState);
      toggle.setAttribute("aria-expanded", String(nextState));
      toggle.setAttribute(
        "aria-label",
        nextState ? "Close navigation" : "Open navigation",
      );
    });

    menu.querySelectorAll("a").forEach((link) => {
      link.addEventListener("click", closeMenu);
    });

    document.addEventListener("keydown", (event) => {
      if (event.key === "Escape") closeMenu();
    });

    document.addEventListener("click", (event) => {
      if (!menu.classList.contains("is-open")) return;
      if (menu.contains(event.target) || toggle.contains(event.target)) return;
      closeMenu();
    });
  }

  function renderStoreButtons() {
    document.querySelectorAll("[data-store-buttons]").forEach((mount) => {
      mount.innerHTML = storeButtons();
    });
  }

  function renderStableReleaseCtas() {
    document.querySelectorAll("[data-stable-release-cta]").forEach((mount) => {
      const copy =
        stableReleaseCta[mount.dataset.stableReleaseCta] ||
        stableReleaseCta.home;
      mount.outerHTML = `
        <section class="section final-cta">
          <div class="shell cta-grid">
            <div>
              <p class="section-label">${copy.label}</p>
              <h2>${copy.title}</h2>
              <p>${copy.text}</p>
            </div>
            <div class="store-actions" data-store-buttons aria-label="Download SDAI"></div>
          </div>
        </section>
      `;
    });
  }

  function initScreenshotSlider() {
    const slider = document.querySelector("[data-screenshot-slider]");
    if (!slider) return;

    const track = slider.querySelector("[data-slider-track]");
    const slides = Array.from(slider.querySelectorAll(".screenshot-slide"));
    const prev = slider.querySelector("[data-slider-prev]");
    const next = slider.querySelector("[data-slider-next]");
    const progress = slider.querySelector("[data-slider-progress]");
    const duration = 10000;
    let index = 0;
    let timer = 0;
    let paused = false;

    if (!track || slides.length < 2 || !progress) return;

    const isPointerInside = () =>
      slider.matches(":hover") || slider.contains(document.activeElement);

    const resetProgress = () => {
      progress.style.transition = "none";
      progress.style.width = "0";
    };

    const startProgress = () => {
      resetProgress();
      window.requestAnimationFrame(() => {
        if (paused) return;
        progress.style.transition = `width ${duration}ms linear`;
        progress.style.width = "100%";
      });
    };

    const schedule = () => {
      window.clearTimeout(timer);
      if (paused || isPointerInside()) {
        resetProgress();
        return;
      }
      startProgress();
      timer = window.setTimeout(() => {
        if (isPointerInside()) {
          paused = true;
          resetProgress();
          return;
        }
        goTo(index + 1);
      }, duration);
    };

    function goTo(nextIndex) {
      index = (nextIndex + slides.length) % slides.length;
      track.style.transform = `translateX(${-index * 100}%)`;
      schedule();
    }

    const pause = () => {
      paused = true;
      window.clearTimeout(timer);
      resetProgress();
    };

    const resume = () => {
      if (!paused) return;
      paused = false;
      schedule();
    };

    prev?.addEventListener("click", () => goTo(index - 1));
    next?.addEventListener("click", () => goTo(index + 1));
    slider.addEventListener("pointerenter", pause);
    slider.addEventListener("pointermove", pause);
    slider.addEventListener("pointerleave", resume);
    slider.addEventListener("mouseenter", pause);
    slider.addEventListener("mousemove", pause);
    slider.addEventListener("mouseleave", resume);
    slider.addEventListener("focusin", pause);
    slider.addEventListener("focusout", (event) => {
      if (slider.contains(event.relatedTarget)) return;
      resume();
    });

    schedule();
  }

  function createFaqItem(item, categoryLabel, isOpen) {
    const details = document.createElement("details");
    details.className = "faq-item";
    details.open = isOpen;

    const summary = document.createElement("summary");
    summary.className = "faq-question";

    const questionCopy = document.createElement("span");
    questionCopy.className = "faq-question-copy";

    const category = document.createElement("span");
    category.className = "faq-category-pill";
    category.textContent = categoryLabel;

    const question = document.createElement("span");
    question.className = "faq-question-text";
    question.textContent = item.question;

    const indicator = document.createElement("span");
    indicator.className = "faq-indicator";
    indicator.setAttribute("aria-hidden", "true");

    questionCopy.append(category, question);
    summary.append(questionCopy, indicator);

    const answer = document.createElement("div");
    answer.className = "faq-answer";

    (item.answer || []).forEach((paragraph) => {
      const node = document.createElement("p");
      node.textContent = paragraph;
      answer.append(node);
    });

    details.append(summary, answer);
    return details;
  }

  function faqItemMatches(item, categoryLabel, query) {
    if (!query) return true;
    const text = [
      categoryLabel,
      item.question,
      ...(item.answer || []),
      ...(item.tags || []),
    ]
      .join(" ")
      .toLowerCase();
    return text.includes(query);
  }

  function addFaqStructuredData(data) {
    const items = (data.categories || []).flatMap((category) => {
      return (category.items || []).map((item) => ({
        "@type": "Question",
        name: item.question,
        acceptedAnswer: {
          "@type": "Answer",
          text: (item.answer || []).join(" "),
        },
      }));
    });

    if (!items.length) return;

    const script = document.createElement("script");
    script.type = "application/ld+json";
    script.textContent = JSON.stringify({
      "@context": "https://schema.org",
      "@type": "FAQPage",
      mainEntity: items,
    });
    document.head.append(script);
  }

  async function initFaqPage() {
    if (document.body.dataset.page !== "faq") return;

    const list = document.querySelector("[data-faq-list]");
    const status = document.querySelector("[data-faq-status]");
    const search = document.querySelector("[data-faq-search]");
    const tabs = document.querySelector("[data-faq-tabs]");
    const updated = document.querySelector("[data-faq-updated]");
    const count = document.querySelector("[data-faq-count]");
    if (!list || !status || !tabs) return;

    try {
      const response = await fetch("faq.json", { cache: "no-store" });
      if (!response.ok)
        throw new Error(`FAQ request failed: ${response.status}`);

      const data = await response.json();
      const categories = Array.isArray(data.categories) ? data.categories : [];
      const total = categories.reduce(
        (sum, category) => sum + (category.items || []).length,
        0,
      );
      let activeCategory = "all";
      let query = "";

      if (updated)
        updated.textContent = formatSupporterDate(data.updated || "");
      if (count) count.textContent = String(total);

      const renderTabs = () => {
        tabs.replaceChildren();

        const allButton = document.createElement("button");
        allButton.type = "button";
        allButton.textContent = "All";
        allButton.setAttribute(
          "aria-pressed",
          String(activeCategory === "all"),
        );
        allButton.addEventListener("click", () => {
          activeCategory = "all";
          renderTabs();
          renderFaq();
        });
        tabs.append(allButton);

        categories.forEach((category) => {
          const button = document.createElement("button");
          button.type = "button";
          button.textContent = category.label;
          button.setAttribute(
            "aria-pressed",
            String(activeCategory === category.id),
          );
          button.addEventListener("click", () => {
            activeCategory = category.id;
            renderTabs();
            renderFaq();
          });
          tabs.append(button);
        });
      };

      const renderFaq = () => {
        const matches = categories.flatMap((category) => {
          if (activeCategory !== "all" && category.id !== activeCategory)
            return [];
          return (category.items || [])
            .filter((item) => faqItemMatches(item, category.label, query))
            .map((item) => ({ item, category }));
        });

        list.replaceChildren();
        matches.forEach(({ item, category }, index) => {
          list.append(createFaqItem(item, category.label, index === 0));
        });

        status.textContent = matches.length
          ? `${matches.length} question${matches.length === 1 ? "" : "s"}`
          : "No matching questions.";
        status.classList.toggle("is-empty", matches.length === 0);
      };

      search?.addEventListener("input", () => {
        query = search.value.trim().toLowerCase();
        renderFaq();
      });

      renderTabs();
      renderFaq();
      addFaqStructuredData(data);
    } catch (error) {
      status.textContent = "FAQ is temporarily unavailable.";
      status.classList.add("is-empty");
    }
  }

  function formatSupporterDate(value) {
    if (typeof value !== "string") return "";
    const parts = value.split("-");
    if (parts.length !== 3) return value;
    const [year, month, day] = parts;
    if (year.length !== 4 || month.length !== 2 || day.length !== 2)
      return value;
    return `${day}.${month}.${year}`;
  }

  function createSupporterCard(supporter) {
    const article = document.createElement("article");
    article.className = "supporter-card";

    const header = document.createElement("div");
    header.className = "supporter-card-head";

    const avatar = document.createElement("span");
    avatar.className = "supporter-avatar";
    avatar.textContent =
      String(supporter.name || "?")
        .trim()
        .charAt(0)
        .toUpperCase() || "?";

    const name = document.createElement("strong");
    name.className = "supporter-name";
    name.textContent = supporter.name || "Someone";

    const date = document.createElement("span");
    date.className = "supporter-date";
    date.textContent = formatSupporterDate(supporter.date);

    header.append(avatar, name, date);
    article.append(header);

    if (supporter.message) {
      const message = document.createElement("p");
      message.className = "supporter-message";
      message.textContent = supporter.message;
      article.append(message);
    }

    return article;
  }

  async function initDonatePage() {
    if (document.body.dataset.page !== "donate") return;

    const list = document.querySelector("[data-supporters-list]");
    const status = document.querySelector("[data-supporters-status]");
    const count = document.querySelector("[data-supporters-count]");
    const latest = document.querySelector("[data-supporters-latest]");
    if (!list) return;

    try {
      const response = await fetch("supporters.json", { cache: "no-store" });
      if (!response.ok)
        throw new Error(`Supporters request failed: ${response.status}`);

      const rawSupporters = await response.json();
      const supporters = rawSupporters
        .filter((item) => item && item.name && item.date)
        .sort((left, right) => {
          const byDate =
            new Date(right.date).getTime() - new Date(left.date).getTime();
          return byDate || Number(right.id || 0) - Number(left.id || 0);
        });

      if (count) count.textContent = String(supporters.length);
      if (latest)
        latest.textContent = supporters[0]
          ? formatSupporterDate(supporters[0].date)
          : "None yet";

      list.replaceChildren();
      supporters.forEach((supporter) => {
        list.append(createSupporterCard(supporter));
      });

      if (status) {
        status.textContent = supporters.length
          ? ""
          : "No supporters are listed yet.";
        status.classList.toggle("is-hidden", supporters.length > 0);
      }
    } catch (error) {
      if (status) {
        status.textContent = "Supporter list is temporarily unavailable.";
        status.classList.remove("is-hidden");
      }
    }
  }

  document.addEventListener("DOMContentLoaded", () => {
    renderHeader();
    renderFooter();
    renderStableReleaseCtas();
    renderStoreButtons();
    setActiveNav();
    initMenu();
    initScreenshotSlider();
    initFaqPage();
    initDonatePage();

    document.querySelectorAll("[data-year]").forEach((node) => {
      node.textContent = String(new Date().getFullYear());
    });
  });
})();
