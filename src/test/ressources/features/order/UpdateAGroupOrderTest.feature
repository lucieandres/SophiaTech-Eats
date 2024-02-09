Feature: Group order management

Background:
  Given The customer "user1" "name1" with the password "password1".
  And He chooses the delivery date "23/11/2025 12:00:00".
  And He chooses the delivery address "930 Rte des Colles, 06410 Biot".
  When He validates the group order.
  Then The group order is registered in the system and we should have 1 order.

  When He chooses the restaurant with id 1.
  And He chooses the menu "Salade César".
  And He validates his suborder.
  Then His order is registered in the group order.

  Given The customer "user4" "name4" with the password "password4".
  When He chooses the restaurant with id 1.
  And He chooses the menu "Salade César".
  And He validates his suborder.
  Then His order is registered in the group order.

Scenario: Updating a group order that is not mine
  Given The customer "user2" "name2" with the password "password2".
  When He update the delivery address of the group order to "1 Place J. Bermond,  06560 Valbonne".
  Then The group order is not updated in the system.
  When He update the delivery date of the group order to "24/11/2025 14:00:00".
  Then The group order is registered in the system and we should have 3 order.

  Scenario: Updating a group order
    Given The customer "user1" "name1" with the password "password1".
    When He update the delivery date of the group order to "24/11/2025 14:00:00".
    Then The group order is updated in the system with the good delivery date.
    And All suborders are updated with the new delivery date.
    When He update the delivery address of the group order to "1 Place J. Bermond,  06560 Valbonne".
    Then The group order is updated in the system with the delivery address "1 Place J. Bermond,  06560 Valbonne".
    And All suborders are updated with the new delivery address.

  Scenario: Canceling a group order that is not mine
    Given The customer "user2" "name2" with the password "password2".
    When He cancels the group order.
    Then The group order is not cancelled in the system.

  Scenario: Canceling a group order
    Given The customer "user1" "name1" with the password "password1".
    When He cancels the group order.
    Then The group order is cancelled in the system.

  Scenario: Canceling a group order in preparation
    Given The customer "user1" "name1" with the password "password1".
    And The order is already in preparation.
    When He cancels the group order.
    Then The group order is not cancelled in the system.
