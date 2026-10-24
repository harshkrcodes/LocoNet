// File: src/utils/errors.js
//
// Spring Boot's default error body doesn't include a message, so errors are
// mapped from the HTTP status instead. Pass `overrides` for screen-specific
// wording, e.g. getApiErrorMessage(err, { 401: 'Invalid email or password.' }).
const DEFAULT_MESSAGES = {
  400: 'Please check the details you entered.',
  401: 'Your session has expired. Please log in again.',
  403: "You don't have permission to do that.",
  404: "We couldn't find what you were looking for.",
  409: 'That already exists.',
  500: 'The server ran into a problem. Please try again.',
};

export function getApiErrorMessage(error, overrides = {}) {
  if (!error?.response) {
    if (error?.code === 'ECONNABORTED') {
      return 'The server took too long to respond. Please try again.';
    }
    return "Can't reach the server. Check your connection and that the backend is running.";
  }

  const status = error.response.status;
  const key = status >= 500 ? 500 : status;
  return overrides[status] ?? DEFAULT_MESSAGES[key] ?? 'Something went wrong. Please try again.';
}
