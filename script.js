/**
 * Drives keyboard-like launcher navigation for the HTML prototype.
 * Updates the highlighted tile, mirrored hero content, and a small selection status message.
 */
(function initializeLauncherMockup() {
  const tiles = Array.from(document.querySelectorAll(".app-tile"));
  const focusTitle = document.getElementById("focus-title");
  const focusCopy = document.getElementById("focus-copy");
  const grid = document.getElementById("app-grid");
  const selectButton = document.getElementById("select-button");
  const moveButtons = Array.from(document.querySelectorAll("[data-move]"));
  let activeIndex = 0;

  /**
   * Returns the tile index after applying a directional step.
   *
   * @param {number} currentIndex Zero-based tile index.
   * @param {number} step Relative move for left, right, up, or down.
   * @returns {number} Clamped tile index that remains inside the launcher grid.
   */
  function getNextIndex(currentIndex, step) {
    const nextIndex = currentIndex + step;
    return Math.max(0, Math.min(nextIndex, tiles.length - 1));
  }

  /**
   * Copies the highlighted tile content into the upper display.
   *
   * @param {HTMLElement} tile Active launcher tile.
   * @returns {void}
   */
  function syncHeroCard(tile) {
    focusTitle.textContent = tile.dataset.title || "";
    focusCopy.textContent = tile.dataset.copy || "";
  }

  /**
   * Applies the active state to a single tile and moves browser focus to it.
   *
   * @param {number} nextIndex Zero-based tile index to highlight.
   * @returns {void}
   */
  function setActiveTile(nextIndex) {
    activeIndex = nextIndex;

    tiles.forEach((tile, index) => {
      const isActive = index === nextIndex;
      tile.classList.toggle("is-active", isActive);
      tile.setAttribute("aria-selected", String(isActive));
    });

    const activeTile = tiles[nextIndex];
    activeTile.focus();
    syncHeroCard(activeTile);
  }

  /**
   * Shows a transient confirmation message beneath the launcher grid.
   *
   * @param {string} label App label shown in the status message.
   * @returns {void}
   * @sideEffects Writes and removes DOM content with a timed callback.
   */
  function showSelectionToast(label) {
    const existingToast = document.querySelector(".selection-toast");
    if (existingToast) {
      existingToast.remove();
    }

    const toast = document.createElement("p");
    toast.className = "selection-toast";
    toast.textContent = `${label} selected. This is where the Android activity handoff would happen.`;
    grid.after(toast);

    window.setTimeout(() => {
      toast.remove();
    }, 1800);
  }

  /**
   * Handles directional navigation requests from the mock D-pad.
   *
   * @param {number} step Relative tile movement.
   * @returns {void}
   */
  function moveSelection(step) {
    setActiveTile(getNextIndex(activeIndex, step));
  }

  tiles.forEach((tile, index) => {
    tile.addEventListener("click", () => setActiveTile(index));
    tile.addEventListener("focus", () => setActiveTile(index));
  });

  moveButtons.forEach((button) => {
    button.addEventListener("click", () => {
      moveSelection(Number(button.dataset.move));
    });
  });

  selectButton.addEventListener("click", () => {
    showSelectionToast(tiles[activeIndex].dataset.title || "App");
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "ArrowLeft") {
      moveSelection(-1);
    } else if (event.key === "ArrowRight") {
      moveSelection(1);
    } else if (event.key === "ArrowUp") {
      moveSelection(-2);
    } else if (event.key === "ArrowDown") {
      moveSelection(2);
    } else if (event.key === "Enter") {
      showSelectionToast(tiles[activeIndex].dataset.title || "App");
    } else {
      return;
    }

    event.preventDefault();
  });

  setActiveTile(activeIndex);
})();
