//
//  Item.swift
//  Muttaqi
//
//  Created by Saad Aziz on 09/02/2026.
//

import Foundation
import SwiftData

@Model
final class Item {
    var timestamp: Date
    
    init(timestamp: Date) {
        self.timestamp = timestamp
    }
}
