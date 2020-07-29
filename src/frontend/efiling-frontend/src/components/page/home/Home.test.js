import React from "react";
import { createMemoryHistory } from "history";
import { MemoryRouter } from "react-router-dom";
import axios from "axios";
import { render, waitFor } from "@testing-library/react";
import MockAdapter from "axios-mock-adapter";

import Home, { saveDataToSessionStorage } from "./Home";
import { getTestData } from "../../../modules/confirmationPopupTestData";
import { getUserDetails } from "../../../modules/userDetails";
import { getDocumentsData } from "../../../modules/documentTestData";
import { getNavigationData } from "../../../modules/navigationTestData";
import { getCourtData } from "../../../modules/courtTestData";
import { generateJWTToken } from "../../../modules/authenticationHelper";

const header = {
  name: "eFiling Frontend",
  history: createMemoryHistory(),
};

const confirmationPopup = getTestData();
const page = { header, confirmationPopup };

describe("Home", () => {
  const submissionId = "abc123";
  const temp = "temp";
  const apiRequest = `/submission/${submissionId}`;
  const getFilingPackagePath = `/submission/${submissionId}/filing-package`;
  const navigation = getNavigationData();
  const documents = getDocumentsData();
  const court = getCourtData();
  const submissionFeeAmount = 25.5;
  const userDetails = getUserDetails();

  window.open = jest.fn();

  const token = generateJWTToken({
    preferred_username: "username@bceid",
    given_name: "User",
    family_name: "Name",
    email: "username@example.com",
  });
  localStorage.setItem("jwt", token);

  let mock;
  beforeEach(() => {
    mock = new MockAdapter(axios);
    sessionStorage.clear();
  });

  const component = (
    <MemoryRouter
      initialEntries={[`?submissionId=${submissionId}&temp=${temp}`]}
    >
      <Home page={page} />
    </MemoryRouter>
  );

  test("Component matches the snapshot when user cso account exists", async () => {
    mock.onGet(apiRequest).reply(200, { userDetails, navigation });
    mock
      .onGet(getFilingPackagePath)
      .reply(200, { documents, court, submissionFeeAmount });

    const { asFragment } = render(component);

    await waitFor(() => {});

    expect(asFragment()).toMatchSnapshot();
    expect(sessionStorage.getItem("cancelUrl")).toEqual("cancelurl.com");
  });

  test("Component matches the snapshot when user cso account does not exist", async () => {
    mock.onGet(apiRequest).reply(200, {
      userDetails: { ...userDetails, accounts: null },
      navigation,
    });

    const { asFragment } = render(component);

    await waitFor(() => {});

    expect(asFragment()).toMatchSnapshot();
    expect(sessionStorage.getItem("cancelUrl")).toEqual("cancelurl.com");
  });

  test("Component matches the snapshot when error encountered, does not attempt to redirect with no errorUrl", async () => {
    mock.onGet(apiRequest).reply(400, { message: "There was an error." });

    const { asFragment } = render(component);

    await waitFor(() => {});

    expect(asFragment()).toMatchSnapshot();
    expect(sessionStorage.getItem("cancelUrl")).toBeFalsy();
    expect(window.open).not.toHaveBeenCalled();
  });

  test("Component matches the snapshot when error encountered", async () => {
    mock.onGet(apiRequest).reply(400, { message: "There was an error." });
    sessionStorage.setItem("errorUrl", "error.com");

    const { asFragment } = render(component);

    await waitFor(() => {});

    expect(asFragment()).toMatchSnapshot();
    expect(window.open).toHaveBeenCalledWith(
      "error.com?status=400&message=There was an error.",
      "_self"
    );
  });

  test("saveDataToSessionStorage saves urls to session storage", () => {
    expect(sessionStorage.getItem("cancelUrl")).toBeFalsy();
    expect(sessionStorage.getItem("successUrl")).toBeFalsy();
    expect(sessionStorage.getItem("errorUrl")).toBeFalsy();

    saveDataToSessionStorage(navigation, userDetails);

    expect(sessionStorage.getItem("cancelUrl")).toEqual("cancelurl.com");
    expect(sessionStorage.getItem("successUrl")).toEqual("successurl.com");
    expect(sessionStorage.getItem("errorUrl")).toBeFalsy();
    expect(sessionStorage.getItem("universalId")).toEqual("123");

    sessionStorage.clear();

    saveDataToSessionStorage(
      {
        ...navigation,
        cancel: { url: "" },
        success: { url: "" },
        error: { url: "error.com" },
      },
      userDetails
    );

    expect(sessionStorage.getItem("cancelUrl")).toBeFalsy();
    expect(sessionStorage.getItem("successUrl")).toBeFalsy();
    expect(sessionStorage.getItem("errorUrl")).toEqual("error.com");
    expect(sessionStorage.getItem("universalId")).toEqual("123");
  });
});
