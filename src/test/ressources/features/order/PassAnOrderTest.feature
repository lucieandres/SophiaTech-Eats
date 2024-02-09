Feature: Order Feature

  Scenario Outline : Passing a simple order with one item
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the delivery date "24/11/2025 12:00:00"
    When he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    When he chooses the restaurant with id <nbItems>
    When he chooses the menu "<menuList>"
    When he validates his order
    Then his order is registered with <nbItems> orders in the system
    And he receives a confirmation notification
    And the order is sent to the restaurant with <nbItems> items
    And the order is in customer's order history

    Examples:
      | menuList                   | nbItems |
      | Steak au poivre            | 1       |
      | Steak au poivre / Tiramisu | 2       |
      | Pizza Pepperoni / Tiramisu | 2       |

  Scenario: Passing a single order on a too early hour
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the delivery date "24/11/2025 7:00:00"
    And he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    And he chooses the restaurant with id 3
    And he chooses the menu "Pizza Pepperoni"
    And he validates his order
    Then an order error message should be printed

  Scenario: Passing a single order on a too late hour
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the delivery date "24/11/2025 21:00:00"
    And he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    And he chooses the restaurant with id 3
    And he chooses the menu "Pizza Pepperoni"
    And he chooses the menu "Tiramisu"
    And he validates his order
    Then an order error message should be printed

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

  Scenario:
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

  Scenario: Using customer type discount
    Given the customer "user1" "name1" with the password "password1"
    Given the customer is a "STUDENT"
    When he chooses the delivery date "24/11/2025 12:00:00"
    When he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    When he chooses the restaurant with id 2
    When he chooses the menu "Café"
    When the restaurant create a student discount for this item
    When he validates his order
    Then his order is registered with 1 orders in the system
    And his order is at student price

  Scenario: Using multiple order at same restaurant discount
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the delivery date "24/11/2025 12:00:00"
    When he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    When he chooses the restaurant with id 2
    When he chooses the menu "Café"
    When it's his 11 order at this restaurant
    When he validates his order
    Then his order is registered with 1 orders in the system
    And his order is at a reduced price

