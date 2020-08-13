export function errorRedirect(errorUrl, error) {
  if (
    errorUrl &&
    error.response &&
    error.response.status &&
    error.response.data &&
    error.response.data.message
  ) {
    sessionStorage.setItem("validExit", true);
    return window.open(
      `${errorUrl}?status=${error.response.status}&message=${error.response.data.message}`,
      "_self"
    );
  }

  return null;
}