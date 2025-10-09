//new user navbutton to create a password
document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('new-user-next');
  if (!btn) return;
  btn.addEventListener('click', () => {
    window.location.assign('./newuser-password.html')
  })
})

document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('newuserbtn');
  if (!btn) return;
  btn.addEventListener('click', () => {
    window.location.assign('./new-user.html')
  })
})
document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('returningbtn');
  if (!btn) return;
  btn.addEventListener('click', () => {
    window.location.assign('./login.html')
  })
})

document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('login-header');
  if (!btn) return;
  btn.addEventListener('click', () => {
    window.location.assign('./index.html')
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

// Helpers
const qs = (s, r = document) => r.querySelector(s);
const qsa = (s, r = document) => [...r.querySelectorAll(s)];
const show = id => { qsa('.panel').forEach(p => p.classList.remove('active')); qs(id).classList.add('active'); };

// Panels
const choiceView = '#choiceView';
const loginView  = '#loginView';
const createView = '#createView';

// Buttons
qs('#btnReturning').addEventListener('click', () => show(loginView));
qs('#btnNew').addEventListener('click', () => show(createView));
qs('#loginBack').addEventListener('click', () => show(choiceView));
qs('#swapToCreate').addEventListener('click', () => show(createView));
qs('#swapToLogin').addEventListener('click', () => show(loginView));

// Forgot password demo (replace with your real handler)
qs('#forgotPassword').addEventListener('click', () => {
  alert('Password reset flow goes here.');
});

// --- Login submission (basic demo validation)
qs('#loginView').addEventListener('submit', (e) => {
  e.preventDefault();
  const user = qs('#loginUsername').value.trim();
  const pass = qs('#loginPassword').value;
  if (!user || !pass) {
    alert('Please enter your username and password.');
    return;
  }
  // TODO: replace with actual auth call
  console.log('Login →', { user });
  alert('Signed in successfully (demo).');
});

// --- Create account stepper ---
let step = 1;
const totalSteps = 3;

const setStep = (n) => {
  step = Math.max(1, Math.min(totalSteps, n));
  // Toggle panes
  qsa('.step-pane').forEach(p => p.classList.remove('is-active'));
  qs(`.step-pane[data-step="${step}"]`).classList.add('is-active');
  // Toggle badges
  qsa('.step').forEach(s => s.classList.remove('is-active'));
  qsa(`.step[data-step="${step}"]`).forEach(s => s.classList.add('is-active'));
};

setStep(1);

// Back buttons
qs('#createBack1').addEventListener('click', () => show(choiceView));
qs('#back2').addEventListener('click', () => setStep(1));
qs('#back3').addEventListener('click', () => setStep(2));

// Next buttons with light validation
qs('#next1').addEventListener('click', () => {
  const f = qs('#firstName').value.trim();
  const l = qs('#lastName').value.trim();
  const d = qs('#dob').value;
  if (!f || !l || !d) return alert('Please complete your name and date of birth.');
  setStep(2);
});

qs('#next2').addEventListener('click', () => {
  const a1 = qs('#address1').value.trim();
  const city = qs('#city').value.trim();
  const state = qs('#state').value.trim();
  const zip = qs('#zip').value.trim();
  if (!a1 || !city || !state || !zip) return alert('Please complete your address.');
  setStep(3);
});

// Create account submission
qs('#createView').addEventListener('submit', (e) => {
  e.preventDefault();

  // Basic validation
  const username = qs('#newUsername').value.trim();
  const pw = qs('#newPassword').value;
  const cpw = qs('#confirmPassword').value;
  const q1 = qs('#q1').value;
  const a1 = qs('#a1').value.trim();

  if (!username || !pw || !cpw || !q1 || !a1) return alert('Please fill out all required security fields.');
  if (pw.length < 8) return alert('Password must be at least 8 characters.');
  if (pw !== cpw) return alert('Passwords do not match.');

  // Gather payload (example)
  const payload = {
    firstName: qs('#firstName').value.trim(),
    lastName: qs('#lastName').value.trim(),
    dob: qs('#dob').value,
    address1: qs('#address1').value.trim(),
    address2: qs('#address2').value.trim(),
    city: qs('#city').value.trim(),
    state: qs('#state').value.trim(),
    zip: qs('#zip').value.trim(),
    username,
    security: [{ question: q1, answer: a1 }]
  };

  // TODO: POST payload to your API
  console.log('Create Account →', payload);
  alert('Account created (demo). You can now sign in.');
  show(loginView);
});
