/* eslint-disable @typescript-eslint/no-unused-vars */
import { i18nBuilder } from "keycloakify/login";
import type { ThemeName } from "../kc.gen";

/** @see: https://docs.keycloakify.dev/features/i18n */
const { useI18n, ofTypeI18n } = i18nBuilder
    .withThemeName<ThemeName>()
    .withExtraLanguages({ /* ... */ })
    .withCustomTranslations({
        // WARNING: You can't import the translation from external files
        en: {
            loginAccountTitle: "Log in",
            usernameOrEmail: "Email",
            username: "Email",
            doLogIn: "Log in",
            noAccount: "Don't have an account?",
            emailInstruction: "Enter your email address and we will send you instructions on how to create a new password."
        },
        // cspell: disable
    })
    .build();

type I18n = typeof ofTypeI18n;

export { useI18n, type I18n };
