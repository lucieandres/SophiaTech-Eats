Feature: Order management

Scenario: Updating a simple order with one item
  Given The customer "user1" "name1" with the password "password1".
  When He chooses the delivery date "24/11/2025 13:00:00".
  And He chooses the delivery address "930 Rte des Colles, 06410 Biot".
  And He chooses the restaurant with id 3.
  And He chooses the menu "Tiramisu".
  And He validates his order.
  And He pays his order.
  Then He can update his order.

  When He update his order with the menu "Pizza Pepperoni".
  Then His order is updated in the system with the menu "Pizza Pepperoni".

  When He update the delivery date to "24/11/2025 14:00:00".
  Then His order is updated in the system with the good delivery date.

  When He update the delivery address to "930 Rte des Colles, 06410 Biot".
  Then His order is updated in the system with the delivery address "930 Rte des Colles, 06410 Biot".

Scenario: Updating a single order with multiple items
  Given The customer "user1" "name1" with the password "password1".
  When He chooses the delivery date "24/11/2025 13:00:00".
  And He chooses the delivery address "930 Rte des Colles, 06410 Biot".
  And He chooses the restaurant with id 3.
  And He chooses the menu "Pizza Pepperoni".
  And He chooses the menu "Tiramisu".
  And He validates his order.
  And He pays his order.
  Then He can update his order.

  When He update his order with the menu "Tiramisu".
  Then His order is updated in the system with the menu "Tiramisu".

  When He update the delivery date to "24/11/2025 14:00:00".
  Then His order is updated in the system with the good delivery date.

  When He update the delivery address to "930 Rte des Colles, 06410 Biot".
  Then His order is updated in the system with the delivery address "930 Rte des Colles, 06410 Biot".
