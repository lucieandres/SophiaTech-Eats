Feature: Edit Restaurant
  Scenario: Edit menu element
    Given a restaurant named "Le Gourmet" with a password "password1"
    When this restaurant want to edit the menu "Steak au poivre" with a new price of 40 and a new name of "Steack au poivre avec frites"
    Then the menu is modified in the system

  Scenario: Add menu element
    Given a restaurant named "Le Gourmet" with a password "password1"
    When this restaurant want to create the new menu "Escalope a la milanaise" with a price of 15
    Then the menu is added in the system

  Scenario: Remove menu element
    Given a restaurant named "Le Gourmet" with a password "password1"
    When this restaurant want to delete the menu "Steack au poivre"
    Then the menu is not present in the system anymore

  Scenario: Edit the menu of a non registered restaurant
    Given a non registered restaurant
    When he want to delete the menu "Steack au poivre"
    Then he get an error
