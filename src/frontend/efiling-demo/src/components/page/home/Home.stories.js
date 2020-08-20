import React from "react";
import { createMemoryHistory } from "history";
import axios from "axios";
import MockAdapter from "axios-mock-adapter";

import Home from "./Home";

export default {
  title: "Home",
  component: Home,
};

const header = {
  name: "eFiling Demo Client",
  history: createMemoryHistory(),
};

const page = { header };

const setRequiredStorage = () => {
  sessionStorage.setItem("demoKeycloakUrl", "demokeycloakexample.com");
  sessionStorage.setItem("demoKeycloakRealm", "demoRealm");
};

const LoadData = (props) => {
  setRequiredStorage();
  const mock = new MockAdapter(axios);
  mock
    .onPost(
      "demokeycloakexample.com/realms/demoRealm/protocol/openid-connect/token"
    )
    .reply(200, { access_token: "token" });
  return props.children({ page });
};

const homeComponent = (data) => <Home page={data.page} />;

export const Default = () => (
  <LoadData>{(data) => homeComponent(data)}</LoadData>
);

export const Mobile = () => (
  <LoadData>{(data) => homeComponent(data)}</LoadData>
);

Mobile.parameters = {
  viewport: {
    defaultViewport: "mobile2",
  },
};
