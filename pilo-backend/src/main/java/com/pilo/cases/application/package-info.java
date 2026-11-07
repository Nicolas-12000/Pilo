/**
 * Named application workflows for the cases bounded context. Controllers delegate here; these types
 * call domain services ({@link com.pilo.cases.CaseService}, etc.) so HTTP stays thin and flows are
 * discoverable by name.
 */
package com.pilo.cases.application;
