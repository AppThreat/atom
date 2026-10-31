import { t } from "i18next";

export const actions = {
  name: ({ t, isMenu }) => t("x"),
  menu: ({ isMenu }) => (isMenu ? t("menu") : "")
};

export function plain() {
  return t("plain");
}
