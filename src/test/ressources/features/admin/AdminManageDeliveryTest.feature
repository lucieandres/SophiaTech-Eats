Feature: Admin Manage Delivery Accounts

  Scenario: Successful Removal of a Delivery Account
    Given I am logged in as an admin
    And there is a delivery account with firstname "John", lastname "Doe", and password "delivery123"
    When I attempt to remove the delivery account
    Then the delivery account should be removed successfully

  Scenario: Unauthorized Removal of a Delivery Account
    Given I not logged in as an admin
    And there is a delivery account with firstname "John", lastname "Doe", and password "delivery123"
    When I attempt to remove the delivery account
    Then I should receive an unauthorized operation message

  Scenario: Successful Addition of a Delivery Account
    Given I am logged in as an admin
    When I attempt to add a delivery account with firstname "John", lastname "Doe", and password "delivery123"
    Then the delivery account should be added successfully

  Scenario: Unauthorized Addition of a Delivery Account
    Given I not logged in as an admin
    When I attempt to add a delivery account with firstname "Jane", lastname "Smith", and password "delivery456"
    Then I should receive an unauthorized operation message