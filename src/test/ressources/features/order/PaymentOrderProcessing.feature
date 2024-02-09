Feature: Payment and order processing

Scenario: Order cancellation by user and refund initiation
  Given the customer "user1" "name1" with the password "password1"
  When he chooses the delivery date "24/11/2025 13:00:00"
  And he chooses the delivery address "930 Rte des Colles, 06410 Biot"
  And he chooses the restaurant with id 3
  And he chooses the menu "Pizza Pepperoni"
  And he chooses the menu "Tiramisu"
  And he validates his order
  And his payment is accepted
  And he cancels the order
  Then the order status is "CANCELED"
  And the refund is initiated
  And he receives a confirmation notification

  Scenario: Passing a single order with payment issue
    Given the customer "user1" "name1" with the password "password1"
    When he chooses the delivery date "24/11/2025 13:00:00"
    When he chooses the delivery address "930 Rte des Colles, 06410 Biot"
    When he chooses the restaurant with id 3
    And he chooses the menu "Pizza Pepperoni"
    And he chooses the menu "Tiramisu"
    And his payment is refused
    And he validates his order
    Then an order error message should be printed