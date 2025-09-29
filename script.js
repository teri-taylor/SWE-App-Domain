//new user navbutton to create a password
document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('new-user-next');
  if (!btn) return;
  btn.addEventListener('click', () => {
    window.location.assign('./newuser-password.html')
  })
})


// if user is admin => admin, if user is manager => manager, if user is accountant => accountant
document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('login-submit');
  if (!btn) return;
  btn.addEventListener('click', () => {
    window.location.assign('./admin.html')
  })
})


// select only one question
document.addEventListener('DOMContentLoaded', () => {
  const selects = document.querySelectorAll('select');

  function updateOptions() {
    const selectedValues = Array.from(selects)
      .map(select => select.value)
      .filter(val => val !== "");

    selects.forEach(select => {
      const currentValue = select.value;

      Array.from(select.options).forEach(option => {
        if (option.value === "") {
          option.disabled = false; // Always allow default/empty
        } else {
          // Disable if selected elsewhere, but not in current select
          option.disabled = selectedValues.includes(option.value) && option.value !== currentValue;
        }
      });
    });
  }

  selects.forEach(select => {
    select.addEventListener('change', updateOptions);
  });

  updateOptions(); // Run once on page load
});



// answer all questions before submitting
document.addEventListener('DOMContentLoaded', () => {
  const selects = document.querySelectorAll('select');
  const submitButton = document.querySelector('.sec-submit');
  const inputs = [
    document.getElementById('q1-answer'),
    document.getElementById('q2-answer'),
    document.getElementById('q3-answer'),
  ];

  // Disable duplicate question selections
  function updateOptions() {
    const selectedValues = Array.from(selects)
      .map(select => select.value)
      .filter(val => val !== "");

    selects.forEach(select => {
      const currentValue = select.value;

      Array.from(select.options).forEach(option => {
        if (option.value === "") {
          option.disabled = false;
        } else {
          option.disabled = selectedValues.includes(option.value) && option.value !== currentValue;
        }
      });
    });
  }

  selects.forEach(select => {
    select.addEventListener('change', updateOptions);
  });

  updateOptions();

  // Validate answers on submit
  submitButton.addEventListener('click', (e) => {
    const allFilled = inputs.every(input => input.value.trim() !== "");

    if (!allFilled) {
      e.preventDefault(); // Stop any form submission or action
      alert('Please answer all 3 questions before submitting.');
    } else {
      alert('Form submitted successfully!');
      // If needed, do additional logic here (submit to server, etc.)
    }
  });
});

//password validation
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('pw-form');
  if (!form) return;

  const pw  = document.getElementById('new-pass');
  const cfm = document.getElementById('confirm-pass');

  function isValidPassword(p) {
    return {
      hasMin:    p.length >= 8,
      hasLetter: /[A-Za-z]/.test(p),
      hasNumber: /\d/.test(p),
      hasSpec:   /[!@#$%^&*(),.?":{}|<>_\-\\[\]\\/;'+=|`~]/.test(p)
    };
  }

  // Hard-stop any default submit first, in capture phase.
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    e.stopImmediatePropagation();

    const p  = pw.value || '';
    const cp = cfm.value || '';
    const t  = isValidPassword(p);
    const ok = t.hasMin && t.hasLetter && t.hasNumber && t.hasSpec && (p === cp);

    if (!ok) {
      const msgs = [];
      if (!t.hasMin)    msgs.push('• Minimum 8 characters');
      if (!t.hasLetter) msgs.push('• Must contain a letter');
      if (!t.hasNumber) msgs.push('• Must contain a number');
      if (!t.hasSpec)   msgs.push('• Must contain a special character');
      if (p !== cp)     msgs.push('• Passwords must match');
      alert('Please fix:\n' + msgs.join('\n'));
      return; // stay on the page
    }

  
    window.location.href = './security-questions.html';
  }, true); // <-- capture phase to beat other listeners

  // Extra belt: block button default too, in case it’s wrapped or overridden elsewhere
  const btn = document.getElementById('new-password-next');
  if (btn) {
    btn.addEventListener('click', (e) => {
      // let the submit handler decide; but prevent stray anchors/defaults
      if (btn.closest('a')) e.preventDefault();
    }, true);
  }
});

