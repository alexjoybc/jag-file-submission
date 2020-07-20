import React from "react";
import { MemoryRouter } from "react-router-dom";
import { createMemoryHistory } from "history";
import axios from "axios";
import MockAdapter from "axios-mock-adapter";
import { getTestData } from "../../../modules/confirmationPopupTestData";
import { getUserDetails } from "../../../modules/userDetails";

import Home from "./Home";

export default {
  title: "Home",
  component: Home
};

const header = {
  name: "E-File Submission",
  history: createMemoryHistory()
};
const confirmationPopup = getTestData();
const page = { header, confirmationPopup };

const submissionId = "abc123";
const temp = "temp";
const apiRequest = `/submission/${submissionId}`;
const apiRequestFiling = `/submission/${submissionId}/filing-package`;
const navigation = {
  cancel: {
    url: "cancelurl.com"
  },
  success: {
    url: "successurl.com"
  },
  error: {
    url: "error.com"
  }
};
const documents = [
  {
    name: "file name 1",
    description: "file description 1",
    type: "file type",
    statutoryFeeAmount: 40
  },
  {
    name: "file name 2",
    description: "file description 2",
    type: "file type",
    statutoryFeeAmount: 0
  }
];
const userDetails = getUserDetails();

sessionStorage.setItem("errorUrl", "error.com");

const LoaderStateData = props => {
  const mock = new MockAdapter(axios);
  window.open = () => {};
  mock.onGet(apiRequest).reply(400, { message: "There was an error" });
  return props.children({ page });
};

const AccountExistsStateData = props => {
  const mock = new MockAdapter(axios);
  mock.onGet(apiRequest).reply(200, { userDetails, navigation });
  mock.onGet(apiRequestFiling).reply(200, { documents });
  return props.children({ page });
};

const NoAccountExistsStateData = props => {
  const mock = new MockAdapter(axios);
  mock.onGet(apiRequest).reply(200, {
    userDetails: { ...userDetails, accounts: null },
    navigation
  });
  return props.children({ page });
};

const homeComponent = data => (
  <MemoryRouter
    initialEntries={[
      { search: `?submissionId=${submissionId}&temp=${temp}`, key: "testKey" }
    ]}
  >
    <Home page={data.page} />
  </MemoryRouter>
);

const loaderComponent = (
  <LoaderStateData>{data => homeComponent(data)}</LoaderStateData>
);

const accountExistsComponent = (
  <AccountExistsStateData>{data => homeComponent(data)}</AccountExistsStateData>
);

const noAccountExistsComponent = (
  <NoAccountExistsStateData>
    {data => homeComponent(data)}
  </NoAccountExistsStateData>
);

export const Loader = () => loaderComponent;

export const AccountExists = () => accountExistsComponent;

export const NoAccountExists = () => noAccountExistsComponent;

export const NoAccountExistsMobile = () => noAccountExistsComponent;

const mobileViewport = {
  parameters: {
    viewport: {
      defaultViewport: "mobile2"
    }
  }
};

NoAccountExistsMobile.story = mobileViewport;
