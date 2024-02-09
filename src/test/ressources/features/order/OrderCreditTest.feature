Feature: Credit from order

  Scenario: Getting discount on a single order with multiple items
    Given the customer "user1" "name1" with the password "password1"
    And he chooses the delivery date "23/10/2025 12:00:00"
    And he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    When he chooses the restaurant with id 3
    And he chooses 3 of the menu "Pizza Pepperoni"
    And he chooses 3 of the menu "Pizza Margherita"
    When he chooses 4 of the menu "Tiramisu"
    And he validates his order
    When his order is finished
    Then he gets a credit on future order

  Scenario: Having credit
    When the customer "user1 name1" logs back in with the password "password1"
    Then he has credit left

  Scenario: Using credit on order
    Given the customer "user1" "name1" with the password "password1"
    Given customer has credit of 9.4 euros from previous order
    When he chooses the delivery date "24/11/2025 12:00:00"
    When he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    When he chooses the restaurant with id 2
    When he chooses the menu "Café"
    When he validates his order
    Then his order is registered with 1 orders in the system
    And he has credit left

  Scenario: Using credit on order
    Given the customer "user1" "name1" with the password "password1"
    Given customer has credit of 6.4 euros from previous order
    When he chooses the delivery date "24/11/2025 12:00:00"
    When he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    When he chooses the restaurant with id 1
    When he chooses the menu "Steak au poivre"
    When he validates his order
    Then his order is registered with 1 orders in the system
    Then he can use his credit to pay part of the order