Feature: Customer browse restaurants menu

  Scenario: Non logged user can see all restaurants menu
    Given A user not logged in
    When He wants to see the menu of every restaurant
    Then He should see all restaurant menus

  Scenario: Non logged user can see the menu of a specific restaurant
    Given A user not logged in
    And A restaurant "Le Gourmet" with id 1 and with a menu
    When He wants to see the menu of "Le Gourmet" with id 1
    Then He should see the menu of "Le Gourmet"

  Scenario: Logged user can see all restaurants menu
    Given A user logged in with firstname "user1", lastname "name1" and password "password1"
    When He wants to see the menu of every restaurant
    Then He should see all restaurant menus

  Scenario:Logged user can see the menu of a specific restaurant
    Given A user logged in with firstname "user1", lastname "name1" and password "password1"
    And A restaurant "Le Gourmet" with id 1 and with a menu
    When He wants to see the menu of "Le Gourmet" with id 1
    Then He should see the menu of "Le Gourmet"
