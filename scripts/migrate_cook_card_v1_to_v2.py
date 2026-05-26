"""
One-shot v1 -> v2 cook-card migrator for the test fixture.

The four shape changes:
  - cookPlan.phases[].bowls[].instruction      string | string[]  ->  InstructionStep[]
  - cookPlan.phases[].standalone[].prep        string | string[]  ->  InstructionStep[]
  - cookPlan.phases[0].steps[].task            string[]           ->  InstructionStep[]
  - cookPlan.phases[2].steps[].task            string | string[]  ->  InstructionStep[]

Every legacy string becomes:
  {"instructionPrefix": "<string>", "ingredients": null}

Run once on sample-cook-card.json, then commit the v2 fixture.
"""

import json
import sys
from pathlib import Path


def to_step_list(value):
    """Wrap a v1 string or string[] value as a list of InstructionStep objects."""
    if value is None:
        return []
    if isinstance(value, str):
        return [{"instructionPrefix": value, "ingredients": None}]
    if isinstance(value, list):
        out = []
        for item in value:
            if isinstance(item, str):
                out.append({"instructionPrefix": item, "ingredients": None})
            elif isinstance(item, dict) and "instructionPrefix" in item:
                # Already v2-shaped; pass through untouched.
                out.append(item)
        return out
    return []


def migrate(card):
    card["schemaVersion"] = "2.0.0"

    phases = card.get("cookPlan", {}).get("phases", [])
    for phase in phases:
        phase_id = phase.get("id")

        if phase_id == "phase-0":
            for step in phase.get("steps", []):
                if "task" in step:
                    step["task"] = to_step_list(step["task"])

        elif phase_id == "phase-1":
            for bowl in phase.get("bowls", []):
                if "instruction" in bowl:
                    bowl["instruction"] = to_step_list(bowl["instruction"])
            for standalone in phase.get("standalone", []):
                if "prep" in standalone:
                    standalone["prep"] = to_step_list(standalone["prep"])

        elif phase_id == "phase-2":
            for step in phase.get("steps", []):
                if "task" in step:
                    step["task"] = to_step_list(step["task"])

    return card


def main():
    target = Path(sys.argv[1])
    raw = target.read_text(encoding="utf-8")
    card = json.loads(raw)
    migrated = migrate(card)
    target.write_text(json.dumps(migrated, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"Migrated {target} -> schemaVersion 2.0.0")


if __name__ == "__main__":
    main()
