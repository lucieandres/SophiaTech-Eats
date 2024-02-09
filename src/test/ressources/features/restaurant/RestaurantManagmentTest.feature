Feature: Restaurant Feature

  Scenario: Updating a menu item in a restaurant
    Given a restaurant
    When I update the menu item "Steak au poivre" to have a price of 27.0
    Then the price of "Steak au poivre" in the menu should be 27.0

  Scenario: Attempting to update an invalid menu item in a restaurant
    Given a restaurant
    When I update an invalid menu item "Invalid Item" to have a price of 10.0
    Then an error message should be printed

  Scenario: The restaurant accept the order
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the restaurant with id 1
    And he chooses 1 of the menu "Steak au poivre"
    And he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    And he chooses the delivery date "24/12/2025 12:30:00"
    And he validates his order
    And the restaurant prepares the order
    And 10 minutes have passed
    Then the order status is "WAITING_DELIVER_ACCEPTANCE"

  Scenario: The restaurant refuse the order
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the restaurant with id 1
    And he chooses 1 of the menu "Steak au poivre"
    And he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    And he chooses the delivery date "24/12/2025 12:30:00"
    And he validates his order
    And the restaurant cancels the order
    Then the order status is "CANCELED"

  Scenario: Updating restaurant operating hours by the restaurant manager
    Given a restaurant
    When the restaurant manager updates the restaurant operating hours to 12:30 to 22:00
    Then the restaurant operating hours are 12:30 to 22:00

